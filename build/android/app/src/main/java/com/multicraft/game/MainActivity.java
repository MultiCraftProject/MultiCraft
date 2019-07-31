package com.multicraft.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.multicraft.game.PreferencesHelper.TAG_BUILD_NUMBER;
import static com.multicraft.game.PreferencesHelper.TAG_CONSENT_ASKED;
import static com.multicraft.game.PreferencesHelper.TAG_COPY_WORLDS;
import static com.multicraft.game.PreferencesHelper.TAG_LAUNCH_TIMES;
import static com.multicraft.game.PreferencesHelper.TAG_SHORTCUT_CREATED;

public class MainActivity extends Activity implements WVersionManager.ActivityListener, CallBackListener, DialogsCallback {
    public final static int REQUEST_CODE = 104;
    private final static String CREATE_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private final static String EXTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    private final static String FILES = EXTERNAL_STORAGE + "/Files.zip";
    private final static String WORLDS = EXTERNAL_STORAGE + "/worlds.zip";
    private final static String GAMES = EXTERNAL_STORAGE + "/games.zip";
    private final static String NOMEDIA = ".nomedia";
    private static final String UPDATE_LINK = "https://raw.githubusercontent.com/MoNTE48/MultiCraft-links/master/Android.json";
    private static final String[] EU_COUNTRIES = new String[]{
            "AT", "BE", "BG", "HR", "CY", "CZ",
            "DK", "EE", "FI", "FR", "DE", "GR",
            "HU", "IE", "IT", "LV", "LT", "LU",
            "MT", "NL", "PL", "PT", "RO", "SK",
            "SI", "ES", "SE", "GB", "IS", "LI", "NO"};
    private static String dataFolder = "/Android/data/com.multicraft.game/files/";
    public static String unzipLocation = EXTERNAL_STORAGE + dataFolder;
    private static String worldPath = EXTERNAL_STORAGE + "/Android/data/mobi.MultiCraft/files/worlds";
    private int height, width;
    private boolean consent;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarIndeterminate;
    private TextView mLoading;
    private ImageView iv;
    private WVersionManager versionManager = null;
    private PreferencesHelper pf;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = 0;
            if (intent != null) {
                progress = intent.getIntExtra(UnzipService.ACTION_PROGRESS, 0);
            }
            if (progress >= 0) {
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(progress);
                }
            } else {
                createNomedia();
                File folder = new File(worldPath);
                if (folder.exists() && !pf.isWorldsCopied())
                    copyWorldsToNewFolder();
                runGame();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        pf = PreferencesHelper.getInstance(this);
        IntentFilter filter = new IntentFilter(UnzipService.ACTION_UPDATE);
        registerReceiver(myReceiver, filter);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        addLaunchTimes();
        askStoragePermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    //helpful utilities
    private void copyWorldsToNewFolder() {
        File source = new File(worldPath);
        File dest = new File(unzipLocation + "worlds");
        try {
            FileUtils.copyDirectory(source, dest);
            pf.saveSettings(TAG_COPY_WORLDS, true);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }

    private void addLaunchTimes() {
        int i = pf.getLaunchTimes();
        i++;
        pf.saveSettings(TAG_LAUNCH_TIMES, i);
    }

    private void createDataFolder() {
        File folder = new File(unzipLocation);
        if (!(folder.exists()))
            folder.mkdirs();
    }

    private void deleteZip(String... filesArray) {
        for (String fileName : filesArray) {
            File file = new File(fileName);
            if (file.exists())
                file.delete();
        }
    }

    private void createNomedia() {
        File myFile = new File(unzipLocation, NOMEDIA);
        if (!myFile.exists())
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
    }

    //interface
    private void addShortcut() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int size = 0;
        if (activityManager != null) {
            size = activityManager.getLauncherLargeIconSize();
        }
        try {
            Drawable icon = getPackageManager().getApplicationIcon(getPackageName());
            Bitmap shortcutIconBitmap = ((BitmapDrawable) icon).getBitmap();
            Bitmap temp;
            if (shortcutIconBitmap.getWidth() == size && shortcutIconBitmap.getHeight() == size)
                temp = shortcutIconBitmap;
            else
                temp = Bitmap.createScaledBitmap(shortcutIconBitmap, size, size, true);
            pf.saveSettings(TAG_SHORTCUT_CREATED, false);
            Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent addIntent = new Intent();
            addIntent.putExtra("duplicate", false);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, temp);
            addIntent.setAction(CREATE_SHORTCUT);
            getApplicationContext().sendBroadcast(addIntent);
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
        }
    }

    private void addImageView(int pos) {
        int marginTop = pos == 0 ? 48 : 288;
        RelativeLayout rl = findViewById(R.id.activity_main);
        iv = new ImageView(this);
        iv.setBackgroundResource(R.drawable.logo);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lp.setMargins(0, marginTop, 0, 0);
        iv.requestLayout();
        iv.setLayoutParams(lp);
        rl.addView(iv);
    }

    private void hideViews() {
        mProgressBar.setVisibility(View.GONE);
        mProgressBarIndeterminate.setVisibility(View.GONE);
        iv.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
    }

    public void getDefaultResolution() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else {
            display.getSize(size);
        }
        height = Math.min(size.x, size.y);
        width = Math.max(size.x, size.y);
    }

    public void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            makeFullScreen();
        }
    }

    private void askGdpr() {
        if (pf.isAskConsent() && isGdprSubject())
            showGdprDialog();
        else {
            consent = true;
            init();
        }
    }

    private void init() {
        mProgressBar = findViewById(R.id.PB1);
        mProgressBarIndeterminate = findViewById(R.id.PB2);
        mLoading = findViewById(R.id.tv_progress_circle);
        Drawable draw = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar);
        mProgressBar.setProgressDrawable(draw);
        RateMe.onStart(this);
        if (pf.isCreateShortcut() && Build.VERSION.SDK_INT < 26)
            addShortcut();
        createDataFolder();
        checkAppVersion();
    }

    //permission block
    private void askStoragePermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (pf.getLaunchTimes() % 3 == 1) {
                    askLocationPermissions();
                } else askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                if (TedPermission.canRequestPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE))
                    askStorageRationalePermissions();
                else askStorageWhenDoNotShow();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .check();
    }

    //storage permissions block
    private void askStorageRationalePermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (pf.getLaunchTimes() % 3 == 1) {
                    askLocationPermissions();
                } else askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                finish();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .setRationaleMessage(R.string.explain)
                .setDeniedMessage(R.string.denied)
                .setDeniedCloseButtonText(R.string.close_game)
                .setGotoSettingButtonText(R.string.settings)
                .check();
    }

    private void askStorageWhenDoNotShow() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (pf.getLaunchTimes() % 3 == 1) {
                    askLocationPermissions();
                } else askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                finish();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .setDeniedMessage(R.string.denied)
                .setDeniedCloseButtonText(R.string.close_game)
                .setGotoSettingButtonText(R.string.settings)
                .check();
    }

    //location permissions block
    private void askLocationPermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                if (TedPermission.canRequestPermission(MainActivity.this, ACCESS_COARSE_LOCATION))
                    askLocationRationalePermissions();
                else askLocationWhenDoNotShow();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(ACCESS_COARSE_LOCATION)
                .check();
    }

    private void askLocationRationalePermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                askGdpr();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(ACCESS_COARSE_LOCATION)
                .setRationaleMessage(R.string.location)
                .check();
    }

    private void askLocationWhenDoNotShow() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if (pf.getLaunchTimes() % 3 == 1) {
                    askLocationPermissions();
                } else askGdpr();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                askGdpr();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(ACCESS_COARSE_LOCATION)
                .setDeniedMessage(R.string.location)
                .setGotoSettingButtonText(R.string.settings)
                .check();
    }

    //game logic
    private void checkRateDialog() {
        if (RateMe.shouldShowRateDialog()) {
            hideViews();
            RateMe.showRateDialog();
            RateMe.setListener(this);
        } else {
            getNativeResolutionAndStart();
        }
    }

    @Override
    public void isShowUpdateDialog(boolean flag) {
        if (flag) {
            versionManager.showDialog();
            versionManager.setListener(this);
        } else {
            checkRateDialog();
        }
    }

    private void checkUrlVersion() {
        versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl(UPDATE_LINK);
        versionManager.checkVersion();

    }

    private void getNativeResolutionAndStart() {
        getDefaultResolution();
        startNative();
    }

    private void runGame() {
        deleteZip(FILES, WORLDS, GAMES);
        pf.saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
        final CheckConnectionTask cct = new CheckConnectionTask(this);
        cct.setListener(this);
        cct.execute();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cct.getStatus() == AsyncTask.Status.RUNNING) {
                    cct.cancel(true);
                    onEvent("CheckConnectionTask", "false");
                }
            }
        }, 2500);
    }

    private void startNative() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("height", height);
        intent.putExtra("width", width);
        intent.putExtra("consent", consent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean isGdprSubject() {
        String locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            locale = getResources().getConfiguration().locale.getCountry();
        }
        return Arrays.asList(EU_COUNTRIES).contains(locale.toUpperCase());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getNativeResolutionAndStart();
    }

    private void prepareToRun(boolean isAll) {
        DeleteTask dt = new DeleteTask();
        dt.setListener(this);
        if (isAll) {
            dt.execute(unzipLocation);
        } else {
            dt.execute(unzipLocation + "builtin", unzipLocation + "games", unzipLocation + "debug.txt");
        }
    }

    private void checkAppVersion() {
        if (pf.getBuildNumber().equals(getString(R.string.ver))) {
            addImageView(1);
            runGame();
        } else if (pf.getBuildNumber().equals("0")) {
            addImageView(0);
            prepareToRun(true);
        } else {
            addImageView(0);
            prepareToRun(false);
        }
    }

    @Override
    public void updateViews(int text, int textVisibility, int progressVisibility) {
        mProgressBarIndeterminate.setVisibility(progressVisibility);
        mLoading.setVisibility(textVisibility);
        mLoading.setText(text);
    }

    @Override
    public void onEvent(String source, String param) {
        if (isFinishing()) return;
        if ("DeleteTask".equals(source)) {
            CopyZipTask cpt = new CopyZipTask(this);
            cpt.setListener(this);
            if (unzipLocation.equals(param)) {
                cpt.execute(FILES, WORLDS, GAMES);
            } else {
                cpt.execute(FILES, GAMES);
            }
        } else if ("CheckConnectionTask".equals(source)) {
            if ("true".equals(param)) {
                checkUrlVersion();
            } else {
                showConnectionDialog();
            }
        }
    }

    private void showGdprDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.CustomLollipopDialogStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.gdpr_dialog, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.gdpr_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        pf.saveSettings(TAG_CONSENT_ASKED, false);
                        dialog.dismiss();
                        consent = true;
                        init();
                    }
                })
                .setNegativeButton(R.string.gdpr_disagree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        pf.saveSettings(TAG_CONSENT_ASKED, false);
                        dialog.dismiss();
                        consent = false;
                        init();
                    }
                });
        TextView tv = dialogView.findViewById(R.id.gdprTextView);
        tv.setText(R.string.gdpr_main_text);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        tv.setMovementMethod(LinkMovementMethod.getInstance());
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
            finish();
        } else if ("ConnectionDialog".equals(source)) {
            startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_CODE);
        } else {
            versionManager.updateNow(versionManager.getUpdateUrl());
            finish();
        }
    }

    @Override
    public void onNegative(String source) {
        if ("RateMe".equals(source)) {
            Toast.makeText(MainActivity.this, R.string.sad, Toast.LENGTH_LONG).show();
            getNativeResolutionAndStart();
        } else if ("ConnectionDialog".equals(source)) {
            startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CODE);
        } else {
            versionManager.ignoreThisVersion();
            checkRateDialog();
        }
    }

    @Override
    public void onNeutral(String source) {
        if ("RateMe".equals(source)) {
            getNativeResolutionAndStart();
        } else if ("ConnectionDialog".equals(source)) {
            getNativeResolutionAndStart();
        } else {
            versionManager.remindMeLater(versionManager.getReminderTimer());
            checkRateDialog();
        }
    }

}
