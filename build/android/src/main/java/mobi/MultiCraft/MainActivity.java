package mobi.MultiCraft;

import android.Manifest;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static mobi.MultiCraft.PreferencesHelper.TAG_BUILD_NUMBER;
import static mobi.MultiCraft.PreferencesHelper.TAG_CONSENT_ASKED;
import static mobi.MultiCraft.PreferencesHelper.TAG_LAUNCH_TIMES;
import static mobi.MultiCraft.PreferencesHelper.TAG_SHORTCUT_CREATED;
import static mobi.MultiCraft.PreferencesHelper.getBuildNumber;
import static mobi.MultiCraft.PreferencesHelper.getLaunchTimes;
import static mobi.MultiCraft.PreferencesHelper.isAskConsent;
import static mobi.MultiCraft.PreferencesHelper.isCreateShortcut;
import static mobi.MultiCraft.PreferencesHelper.isRestored;
import static mobi.MultiCraft.PreferencesHelper.loadSettings;
import static mobi.MultiCraft.PreferencesHelper.saveSettings;

public class MainActivity extends Activity implements WVersionManager.ActivityListener, CallBackListener, DialogsCallback {
    public final static int REQUEST_CODE = 104;
    private final static String TAG = "Error";
    private final static String CREATE_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private final static String FILES = Environment.getExternalStorageDirectory() + "/Files.zip";
    private final static String WORLDS = Environment.getExternalStorageDirectory() + "/worlds.zip";
    private final static String GAMES = Environment.getExternalStorageDirectory() + "/games.zip";
    private final static String NOMEDIA = ".nomedia";
    private final static int COARSE_LOCATION_RESULT = 100;
    private final static int WRITE_EXTERNAL_RESULT = 101;
    private final static int ALL_PERMISSIONS_RESULT = 102;
    private static final String UPDATE_LINK = "https://raw.githubusercontent.com/MoNTE48/MultiCraft-links/master/ver.txt";
    private static final String[] EU_COUNTRIES = new String[]{
            "AT", "BE", "BG", "HR", "CY", "CZ",
            "DK", "EE", "FI", "FR", "DE", "GR",
            "HU", "IE", "IT", "LV", "LT", "LU",
            "MT", "NL", "PL", "PT", "RO", "SK",
            "SI", "ES", "SE", "GB", "IS", "LI", "NO"};
    private static String dataFolder = "/Android/data/mobi.MultiCraft/files/";
    public static String unzipLocation = Environment.getExternalStorageDirectory() + dataFolder;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarIndeterminate;
    private TextView mLoading;
    private ImageView iv;
    private WVersionManager versionManager = null;
    private ConnectionDialogListener connListener;
    private PermissionManager pm = null;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = 0;
            if (intent != null) {
                progress = intent.getIntExtra(UnzipService.ACTION_PROGRESS, 0);
            }
            if (progress >= 0) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(progress);
            } else {
                createNomedia();
                runGame();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        loadSettings(this);
        IntentFilter filter = new IntentFilter(UnzipService.ACTION_UPDATE);
        registerReceiver(myReceiver, filter);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        addLaunchTimes();
        getPermissions();
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
    private void addLaunchTimes() {
        int i = getLaunchTimes();
        i++;
        saveSettings(TAG_LAUNCH_TIMES, i);
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
                Log.e(TAG, "nomedia has not been created: " + e.getMessage());
            }
    }

    //interface
    private void addShortcut() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int size = 0;
        if (activityManager != null) {
            size = activityManager.getLauncherLargeIconSize();
        }
        if (Build.VERSION.SDK_INT < 26) {
            try {
                Drawable icon = getPackageManager().getApplicationIcon(getPackageName());
                Bitmap shortcutIconBitmap = ((BitmapDrawable) icon).getBitmap();
                Bitmap temp;
                if (shortcutIconBitmap.getWidth() == size && shortcutIconBitmap.getHeight() == size)
                    temp = shortcutIconBitmap;
                else
                    temp = Bitmap.createScaledBitmap(shortcutIconBitmap, size, size, true);
                saveSettings(TAG_SHORTCUT_CREATED, false);
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
                Log.e(TAG, "Shortcut cannot be created");
            }
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
        if (isAskConsent() && isGdprSubject())
            showGdprDialog();
        else {
//            initAd(this, true);
            init();
        }
    }

    private void init() {
        RateMe.onStart(this);
        if (isCreateShortcut())
            addShortcut();
        mProgressBar = findViewById(R.id.PB1);
        mProgressBarIndeterminate = findViewById(R.id.PB2);
        mLoading = findViewById(R.id.tv_progress_circle);
        Drawable draw = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar);
        mProgressBar.setProgressDrawable(draw);
        connListener = new ConnectionDialogListener();
        createDataFolder();
        checkAppVersion();
    }

    //permission block
    private void getPermissions() {
        pm = new PermissionManager(this);
        String[] permList = pm.requestPermissions();
        if (permList.length > 0) {
            ActivityCompat.requestPermissions(this, permList, ALL_PERMISSIONS_RESULT);
        } else {
            askGdpr();
        }
    }

    private void requestPermissionAfterExplain() {
        Toast.makeText(this, R.string.explain, Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_RESULT);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionAfterExplain();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_RESULT);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_RESULT:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    askGdpr();
                } else {
                    requestStoragePermission();
                }
                break;
            case COARSE_LOCATION_RESULT:
                break;
            case ALL_PERMISSIONS_RESULT:
                for (String perms : PermissionManager.permissionsToRequest) {
                    if (!pm.hasPermission(perms)) {
                        PermissionManager.permissionsRejected.add(perms);
                    }
                }
                if (PermissionManager.permissionsRejected.size() == 0) {
                    askGdpr();
                } else if (!Arrays.asList(PermissionManager.permissionsRejected.toArray()).contains(WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.location, Toast.LENGTH_SHORT).show();
                    askGdpr();
                } else {
                    requestStoragePermission();
                }
                break;
        }
    }

    //game logic
    private void checkRateDialog() {
        if (RateMe.shouldShowRateDialog()) {
            hideViews();
            RateMe.showRateDialog(this);
            RateMe.setListener(this);
        } else {
//            startBillingActivity();
            startNative();
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

    private void runGame() {
        deleteZip(FILES, WORLDS, GAMES);
        CheckConnectionTask cct = new CheckConnectionTask(this);
        cct.setListener(this);
        cct.execute();
    }

//    private void startBillingActivity() {
//        Intent intent = new Intent(this, BillingActivity.class);
//        startActivityForResult(intent, REQUEST_CODE);
//    }

    private void startNative() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("density", getResources().getDisplayMetrics().density);
        intent.putExtra("width", getResources().getDisplayMetrics().widthPixels);
        intent.putExtra("height", getResources().getDisplayMetrics().heightPixels);
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
        startNative();
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
        if (!isRestored() && getBuildNumber().equals(getString(R.string.ver))) {
            addImageView(1);
            runGame();
        } else if (getBuildNumber().equals("0")) {
            addImageView(0);
            saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
            prepareToRun(true);
        } else {
            addImageView(0);
            saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
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
        View dialogView = inflater.inflate(R.layout.gdpr_dialog, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.gdpr_agree, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        saveSettings(TAG_CONSENT_ASKED, false);
                        dialog.dismiss();
//                        initAd(MainActivity.this, true);
                        init();
                    }
                })
                .setNegativeButton(R.string.gdpr_disagree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveSettings(TAG_CONSENT_ASKED, false);
                        dialog.dismiss();
//                        initAd(MainActivity.this, false);
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

    void showConnectionDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.CustomLollipopDialogStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setMessage(getString(R.string.conn_message));

        builder.setPositiveButton(getString(R.string.conn_wifi), connListener);
        builder.setNegativeButton(getString(R.string.conn_mobile), connListener);
        builder.setNeutralButton(getString(R.string.ignore), connListener);

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (!isFinishing()) {
            dialog.show();
        }
    }

    @Override
    public void onPositive(String source) {
        if ("RateMe".equals(source)) {
            finish();
        } else {
            versionManager.updateNow(versionManager.getUpdateUrl());
            finish();
        }
    }

    @Override
    public void onNegative(String source) {
        if ("RateMe".equals(source)) {
            Toast.makeText(MainActivity.this, R.string.sad, Toast.LENGTH_LONG).show();
//            startBillingActivity();
            startNative();
        } else {
            versionManager.ignoreThisVersion();
            checkRateDialog();
        }
    }

    @Override
    public void onCancelled(String source) {
        if ("RateMe".equals(source)) {
//            startBillingActivity();
            startNative();
        } else {
            versionManager.remindMeLater(versionManager.getReminderTimer());
            checkRateDialog();
        }
    }

    private class ConnectionDialogListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), REQUEST_CODE);
                    break;
                case AlertDialog.BUTTON_NEUTRAL:
                    startNative();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), REQUEST_CODE);
                    break;
            }
        }
    }
}