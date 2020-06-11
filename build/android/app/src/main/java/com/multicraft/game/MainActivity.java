package com.multicraft.game;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugsnag.android.Bugsnag;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.multicraft.game.callbacks.CallBackListener;
import com.multicraft.game.callbacks.DialogsCallback;
import com.multicraft.game.helpers.AlertDialogHelper;
import com.multicraft.game.helpers.PermissionHelper;
import com.multicraft.game.helpers.PreferencesHelper;
import com.multicraft.game.helpers.Utilities;
import com.multicraft.game.helpers.VersionManagerHelper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.multicraft.game.helpers.ApiLevelHelper.isGreaterOrEqual;
import static com.multicraft.game.helpers.ApiLevelHelper.isGreaterOrEqualLollipop;
import static com.multicraft.game.helpers.ApiLevelHelper.isGreaterOrEqualOreo;
import static com.multicraft.game.helpers.ApiLevelHelper.isGreaterOrEqualQ;
import static com.multicraft.game.helpers.OldVersionHelper.REQUEST_UNINSTALL;
import static com.multicraft.game.helpers.OldVersionHelper.isOldVersionExists;
import static com.multicraft.game.helpers.OldVersionHelper.uninstallOldVersion;
import static com.multicraft.game.helpers.PreferencesHelper.TAG_BUILD_NUMBER;
import static com.multicraft.game.helpers.PreferencesHelper.TAG_CONSENT_ASKED;
import static com.multicraft.game.helpers.PreferencesHelper.TAG_LAUNCH_TIMES;
import static com.multicraft.game.helpers.Utilities.addShortcut;
import static com.multicraft.game.helpers.Utilities.deleteFiles;
import static com.multicraft.game.helpers.Utilities.getIcon;
import static com.multicraft.game.helpers.Utilities.getStoreUrl;
import static com.multicraft.game.helpers.Utilities.isArm64;
import static com.multicraft.game.helpers.Utilities.makeFullScreen;

public class MainActivity extends AppCompatActivity implements CallBackListener, DialogsCallback {
    public final static Map<String, String> zipLocations = new HashMap<>();
    private final static String SERVER_URL = "http://updates.multicraft.world/";
    public final static String UPDATE_LINK = SERVER_URL + "Android.json";
    private final static int REQUEST_CONNECTION = 104;
    private final static int REQUEST_UPDATE = 102;
    private final static List<String> EU_COUNTRIES = Arrays.asList(
            "AT", "BE", "BG", "HR", "CY", "CZ",
            "DK", "EE", "FI", "FR", "DE", "GR",
            "HU", "IE", "IT", "LV", "LT", "LU",
            "MT", "NL", "PL", "PT", "RO", "SK",
            "SI", "ES", "SE", "GB", "IS", "LI", "NO");
    private static String FILES, WORLDS, GAMES, CACHE;
    private final String versionName = BuildConfig.VERSION_NAME;
    private String unzipLocation, appData;
    private int height, width;
    private boolean consent;
    private ProgressBar mProgressBar, mProgressBarIndeterminate;
    private TextView mLoading;
    private VersionManagerHelper versionManagerHelper = null;
    private PreferencesHelper pf;
    private AppUpdateManager appUpdateManager;
    final InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            if (mProgressBar != null) {
                int progress = (int) (state.bytesDownloaded() * 100 / state.totalBytesToDownload());
                showProgress(R.string.downloading, R.string.downloadingp, progress);
            }
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            appUpdateManager.completeUpdate();
        }
    };
    private Disposable connectionSub;
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = 0;
            if (intent != null)
                progress = intent.getIntExtra(UnzipService.ACTION_PROGRESS, 0);
            if (progress >= 0) {
                if (mProgressBar != null) {
                    showProgress(R.string.loading, R.string.loadingp, progress);
                }
            } else {
                if (isOldVersionExists(MainActivity.this, unzipLocation)) {
                    showUninstallDialog();
                } else {
                    runGame();
                }
            }
        }
    };
    private Disposable versionManagerSub;
    private Disposable cleanSub;
    private Disposable copySub;
    private Task<AppUpdateInfo> appUpdateInfoTask;

    // helpful utilities
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        pf = PreferencesHelper.getInstance(this);
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        IntentFilter filter = new IntentFilter(UnzipService.ACTION_UPDATE);
        registerReceiver(myReceiver, filter);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        addLaunchTimes();
        PermissionHelper permission = new PermissionHelper(this);
        permission.setListener(this);
        permission.askPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen(this);
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
                appUpdateManager.completeUpdate();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent abrupt interruption when copy game files from assets
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionSub != null) connectionSub.dispose();
        if (versionManagerSub != null) versionManagerSub.dispose();
        if (cleanSub != null) cleanSub.dispose();
        if (copySub != null) copySub.dispose();
        appUpdateManager.unregisterListener(listener);
        unregisterReceiver(myReceiver);
    }

    private void initZipLocations() throws Exception {
        /* Temporary solution! */
        // sdcard
        File externalDir = getExternalFilesDir(null); // /sdcard/Android/data/*
        unzipLocation = externalDir + File.separator;
        if (externalDir == null) {
            externalDir = Environment.getExternalStorageDirectory();  // /sdcard
            unzipLocation = externalDir + File.separator + "Android/data/com.multicraft.game" + File.separator;
            Bugsnag.leaveBreadcrumb("The show begins... externalDir = " + externalDir);
        }
        if (externalDir == null) {
            Bugsnag.leaveBreadcrumb("PIZDEC... The show is canceled. externalDir = null");
            throw new Exception("WTF");
        }
        // data/data
        appData = getFilesDir() + File.separator;
        // cache
        File cacheDir = getCacheDir(); // /data/data/*/cache/
        String cachePath = cacheDir + File.separator;
        if (cacheDir == null) {
            cachePath = unzipLocation + "cache" + File.separator;
            Bugsnag.leaveBreadcrumb("The show begins... cachePath = " + cachePath);
        }

        FILES = cachePath + "Files.zip";
        WORLDS = cachePath + "worlds.zip";
        GAMES = cachePath + "games.zip";
        CACHE = cachePath + "cache.zip";
        zipLocations.put(FILES, appData);
        zipLocations.put(GAMES, appData);
        zipLocations.put(WORLDS, unzipLocation);
        zipLocations.put(CACHE, unzipLocation);
    }

    private void addLaunchTimes() {
        int i = pf.getLaunchTimes();
        i++;
        pf.saveSettings(TAG_LAUNCH_TIMES, i);
    }

    private void createDataFolder() {
        File folder = new File(unzipLocation);
        if (!folder.mkdirs() && !folder.isDirectory())
            Bugsnag.leaveBreadcrumb(folder + " (unzipLocation) folder was not created");
    }

    // interface
    private void showProgress(int textMessage, int progressMessage, int progress) {
        if (mProgressBar.getVisibility() == View.GONE)
            updateViews(textMessage, View.VISIBLE, View.GONE, View.VISIBLE);
        else if (progress > 0) {
            mLoading.setText(String.format(getResources().getString(progressMessage), progress));
            mProgressBar.setProgress(progress);
            // colorize the progress bar
            Drawable progressDrawable = ((LayerDrawable)
                    mProgressBar.getProgressDrawable()).getDrawable(1);
            int color = Color.rgb(255 - progress * 2, progress * 2, 25);
            if (isGreaterOrEqualQ())
                progressDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
            else
                progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    // real screen resolution
    private void getDefaultResolution() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (isGreaterOrEqual(Build.VERSION_CODES.JELLY_BEAN_MR1))
            display.getRealSize(size);
        else
            display.getSize(size);
        height = Math.min(size.x, size.y);
        width = Math.max(size.x, size.y);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            makeFullScreen(this);
    }

    // GDPR check
    private void askGdpr() {
        if (pf.isAskConsent() && isGdprSubject())
            showGdprDialog();
        else {
            consent = true;
            startNative();
        }
    }

    private void init() {
        try {
            initZipLocations();
        } catch (Exception e) {
            showRestartDialog("");
        }
        mProgressBar = findViewById(R.id.PB1);
        mProgressBarIndeterminate = findViewById(R.id.PB2);
        mLoading = findViewById(R.id.tv_progress);
        RateMe.onStart(this);
        if (!pf.isCreateShortcut() && !isGreaterOrEqualOreo())
            addShortcut(this);
        checkAppVersion();
    }

    // game logic
    private void checkRateDialog() {
        if (RateMe.shouldShowRateDialog()) {
            updateViews(R.string.empty, View.GONE, View.GONE, View.GONE);
            RateMe.showRateDialog();
            RateMe.setListener(this);
        } else
            askGdpr();
    }

    void showUpdateDialog() {
        AlertDialogHelper dialogHelper = new AlertDialogHelper(this);
        dialogHelper.setListener(this);
        dialogHelper.setIcon(getIcon(this));
        dialogHelper.setTitle(getString(R.string.available));
        dialogHelper.setMessage(Html.fromHtml(versionManagerHelper.getMessage(), null, versionManagerHelper.getCustomTagHandler()));
        dialogHelper.setButtonPositive(getString(R.string.update));
        dialogHelper.setButtonNeutral(getString(R.string.later));
        dialogHelper.showAlert("VersionManager");
    }

    public void startUpdate() {
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, REQUEST_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    Bugsnag.notify(e);
                    showUpdateDialog();
                }
            } else {
                showUpdateDialog();
            }
        });
        appUpdateInfoTask.addOnFailureListener(e -> {
            Bugsnag.notify(e);
            showUpdateDialog();
        });
    }

    private void checkUrlVersion() {
        versionManagerHelper = new VersionManagerHelper(this);
        if (versionManagerHelper.isCheckVersion())
            versionManagerSub = Observable.fromCallable(() -> versionManagerHelper.getJson())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(3000, TimeUnit.MILLISECONDS)
                    .subscribe(result -> isShowDialog(versionManagerHelper.isShow(result)),
                            throwable -> runOnUiThread(() -> isShowDialog(false)));
        else isShowDialog(false);
    }

    private void runGame() {
        deleteFiles(Arrays.asList(FILES, WORLDS, GAMES, CACHE));
        pf.saveSettings(TAG_BUILD_NUMBER, versionName);
        connectionSub = checkConnection();
    }

    private void showUninstallDialog() {
        AlertDialogHelper dialogHelper = new AlertDialogHelper(this);
        dialogHelper.setListener(this);
        dialogHelper.setMessage(getString(R.string.del_message));
        dialogHelper.setButtonPositive(getString(android.R.string.ok));
        dialogHelper.showAlert("UninstallDialog");
    }

    private void startNative() {
        getDefaultResolution();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("height", height);
        intent.putExtra("width", width);
        intent.putExtra("consent", consent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean isGdprSubject() {
        String locale;
        if (isGreaterOrEqual(Build.VERSION_CODES.N))
            locale = getResources().getConfiguration().getLocales().get(0).getCountry();
        else
            locale = getResources().getConfiguration().locale.getCountry();
        return EU_COUNTRIES.contains(locale.toUpperCase());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UNINSTALL)
            runGame();
        else if (requestCode == REQUEST_CONNECTION) {
            checkUrlVersion();
        } else if (requestCode == REQUEST_UPDATE) {
            if (resultCode == RESULT_OK)
                appUpdateManager.registerListener(listener);
            else
                checkRateDialog();
        }
    }

    private void cleanUpOldFiles(boolean isAll) {
        updateViews(R.string.preparing, View.VISIBLE, View.VISIBLE, View.GONE);
        List<String> filesList;
        if (isAll)
            filesList = Collections.singletonList(unzipLocation);
        else {
            filesList = Arrays.asList(unzipLocation + "cache",
                    unzipLocation + "builtin", appData + "builtin",
                    unzipLocation + "games", appData + "games",
                    unzipLocation + "debug.txt");
        }
        cleanSub = Observable.fromCallable(() -> deleteFiles(filesList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> startCopy(isAll));
    }

    private void checkAppVersion() {
        if (pf.getBuildNumber().equals(versionName)) {
            mProgressBarIndeterminate.setVisibility(View.VISIBLE);
            runGame();
        } else if (pf.getBuildNumber().equals("0")) {
            createDataFolder();
            cleanUpOldFiles(true);
        } else {
            createDataFolder();
            cleanUpOldFiles(false);
        }
    }

    public void updateViews(int text, int textVisibility, int progressIndetermVisibility, int progressVisibility) {
        mLoading.setText(text);
        mLoading.setVisibility(textVisibility);
        mProgressBarIndeterminate.setVisibility(progressIndetermVisibility);
        mProgressBar.setVisibility(progressVisibility);
    }

    public void isShowDialog(boolean flag) {
        if (flag) {
            updateViews(R.string.loading, View.VISIBLE, View.VISIBLE, View.GONE);
            if (isGreaterOrEqualLollipop())
                startUpdate();
            else
                showUpdateDialog();
        } else
            checkRateDialog();
    }

    private Disposable checkConnection() {
        return Observable.fromCallable(Utilities::isReachable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(4000, TimeUnit.MILLISECONDS)
                .subscribe(result -> {
                            if (result) checkUrlVersion();
                            else showConnectionDialog();
                        },
                        throwable -> runOnUiThread(this::showConnectionDialog));
    }

    private void startCopy(boolean isAll) {
        String[] zips;
        if (isAll) {
            if (isArm64())
                zips = new String[]{FILES, WORLDS, GAMES, CACHE};
            else
                zips = new String[]{FILES, WORLDS, GAMES};
        } else {
            if (isArm64())
                zips = new String[]{FILES, GAMES, CACHE};
            else
                zips = new String[]{FILES, GAMES};
        }
        copySub = Completable.fromAction(() -> copyAssets(zips))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> startUnzipService(zips),
                        throwable -> {
                            if (Objects.requireNonNull(throwable.getLocalizedMessage()).contains("ENOSPC"))
                                showRestartDialog("ENOSPC");
                            else showRestartDialog("UKNWN");
                        });
    }

    private void copyAssets(String[] zips) throws IOException {
        for (String zipName : zips) {
            String filename = FilenameUtils.getName(zipName);
            try (InputStream in = getAssets().open(filename)) {
                FileUtils.copyInputStreamToFile(in, new File(zipName));
            }
        }
    }

    private void startUnzipService(String[] file) {
        Intent intent = new Intent(this, UnzipService.class);
        intent.putExtra(UnzipService.EXTRA_KEY_IN_FILE, file);
        startService(intent);
    }

    private void showRestartDialog(final String source) {
        String message;
        if ("ENOSPC".equals(source))
            message = getString(R.string.no_space);
        else
            message = getString(R.string.restart);
        final AlertDialogHelper dialogHelper = new AlertDialogHelper(this);
        dialogHelper.setListener(this);
        dialogHelper.setMessage(message);
        dialogHelper.setButtonPositive(getString(android.R.string.ok));
        dialogHelper.showAlert("Restart");
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 1337;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (mgr != null)
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
        System.exit(0);
    }

    @Override
    public void onEvent(boolean isContinue) {
        if (isFinishing()) return;
        if (isContinue) init();
        else finish();
    }

    private void showGdprDialog() {
        AlertDialogHelper dialogHelper = new AlertDialogHelper(this);
        dialogHelper.setListener(this);
        dialogHelper.setIcon(getIcon(this));
        dialogHelper.setTitle(getString(R.string.app_name));
        TextView tv = new TextView(this);
        tv.setText(R.string.gdpr_main_text);
        tv.setPadding(20, 0, 20, 0);
        tv.setGravity(Gravity.CENTER);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        dialogHelper.setTV(tv);
        dialogHelper.setButtonPositive(getString(R.string.gdpr_agree));
        dialogHelper.setButtonNegative(getString(R.string.gdpr_disagree));
        dialogHelper.showAlert("GdprDialog");
    }

    private void showConnectionDialog() {
        AlertDialogHelper dialogHelper = new AlertDialogHelper(this);
        dialogHelper.setListener(this);
        dialogHelper.setMessage(getString(R.string.conn_message));
        dialogHelper.setButtonPositive(getString(R.string.conn_wifi));
        dialogHelper.setButtonNegative(getString(R.string.conn_mobile));
        dialogHelper.setButtonNeutral(getString(R.string.ignore));
        dialogHelper.showAlert("ConnectionDialog");
    }

    @Override
    public void onPositive(String source) {
        if ("RateMe".equals(source)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getStoreUrl()));
            startActivity(intent);
            Toast.makeText(this, R.string.thank, Toast.LENGTH_LONG).show();
            finish();
        } else if ("Restart".equals(source))
            restartApp();
        else if ("ConnectionDialog".equals(source))
            try {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_CONNECTION);
            } catch (ActivityNotFoundException e) {
                Bugsnag.notify(e);
                askGdpr();
            }
        else if ("UninstallDialog".equals(source))
            uninstallOldVersion(this);
        else if ("GdprDialog".equals(source)) {
            pf.saveSettings(TAG_CONSENT_ASKED, false);
            consent = true;
            startNative();
        } else {
            versionManagerHelper.updateNow(versionManagerHelper.getUpdateUrl());
            finish();
        }
    }

    @Override
    public void onNegative(String source) {
        if ("RateMe".equals(source))
            askGdpr();
        else if ("ConnectionDialog".equals(source))
            try {
                startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CONNECTION);
            } catch (ActivityNotFoundException e) {
                Bugsnag.notify(e);
                askGdpr();
            }
        else if ("GdprDialog".equals(source)) {
            pf.saveSettings(TAG_CONSENT_ASKED, false);
            consent = false;
            startNative();
        } else
            checkRateDialog();
    }

    @Override
    public void onNeutral(String source) {
        if ("RateMe".equals(source))
            askGdpr();
        else if ("ConnectionDialog".equals(source))
            askGdpr();
        else {
            versionManagerHelper.remindMeLater();
            checkRateDialog();
        }
    }
}
