package mobi.MultiCraft;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static mobi.MultiCraft.PreferencesHelper.TAG_BUILD_NUMBER;
import static mobi.MultiCraft.PreferencesHelper.TAG_LAUNCH_TIMES;
import static mobi.MultiCraft.PreferencesHelper.TAG_SHORTCUT_CREATED;
import static mobi.MultiCraft.PreferencesHelper.getBuildNumber;
import static mobi.MultiCraft.PreferencesHelper.getLaunchTimes;
import static mobi.MultiCraft.PreferencesHelper.isCreateShortcut;
import static mobi.MultiCraft.PreferencesHelper.isRestored;
import static mobi.MultiCraft.PreferencesHelper.loadSettings;
import static mobi.MultiCraft.PreferencesHelper.saveSettings;

public class MainActivity extends Activity implements WVersionManager.ActivityListener {
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

    private String dataFolder = "/Android/data/mobi.MultiCraft/files/";
    private String unzipLocation = Environment.getExternalStorageDirectory() + dataFolder;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarIndeterminate;
    private TextView mLoading;
    private ImageView iv;
    private WVersionManager versionManager = null;

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
//        if (!isAdsDisabled())
//            initAd(this);
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

    private void deleteFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                Log.e(TAG, "delete files failed: " + e.getLocalizedMessage());
            }
        }
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
        int size = activityManager.getLauncherLargeIconSize();
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

    private void addImageView(int pos) {
        int marginTop = pos == 0 ? 48 : 288;
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_main);
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

    public void init() {
        RateMe.onStart(this);
        if (isCreateShortcut())
            addShortcut();
        mProgressBar = (ProgressBar) findViewById(R.id.PB1);
        mProgressBarIndeterminate = (ProgressBar) findViewById(R.id.PB2);
        mLoading = (TextView) findViewById(R.id.tv_progress_circle);
        Drawable draw = ContextCompat.getDrawable(this, R.drawable.custom_progress_bar);
        mProgressBar.setProgressDrawable(draw);
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
            init();
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
                    init();
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
                    init();
                } else if (!Arrays.asList(PermissionManager.permissionsRejected.toArray()).contains(WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.location, Toast.LENGTH_SHORT).show();
                    init();
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
            RateMe.setCallback(new RateMe.Callback() {
                @Override
                public void onPositive() {
                    finish();
                }

                @Override
                public void onNegative() {
                    Toast.makeText(MainActivity.this, R.string.sad, Toast.LENGTH_LONG).show();
                    startGameActivity();
                }

                @Override
                public void onCancelled() {
                    startGameActivity();
                }
            });
        } else {
            startGameActivity();
        }
    }

    @Override
    public void isShowUpdateDialog(boolean flag) {
        if (flag) {
            versionManager.showDialog();
            versionManager.setCallback(new WVersionManager.Callback() {
                @Override
                public void onPositive() {
                    versionManager.updateNow(versionManager.getUpdateUrl());
                    finish();
                }

                @Override
                public void onNegative() {
                    versionManager.ignoreThisVersion();
                    checkRateDialog();
                }

                @Override
                public void onRemind() {
                    versionManager.remindMeLater(versionManager.getReminderTimer());
                    checkRateDialog();
                }
            });
        } else {
            checkRateDialog();
        }
    }

    private void checkUrlVersion() {
        versionManager = new WVersionManager(this);
        versionManager.setVersionContentUrl(UPDATE_LINK);
        versionManager.checkVersion();

    }

    public void runGame() {
        deleteZip(FILES, WORLDS, GAMES);
        Intent intent = new Intent(this, BillingActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }


    private void startGameActivity() {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void startUnzipService(String[] file) throws IOException {
        // Start MyIntentService
        Intent intentMyIntentService = new Intent(this, UnzipService.class);
        intentMyIntentService.putExtra(UnzipService.EXTRA_KEY_IN_FILE, file);
        intentMyIntentService.putExtra(UnzipService.EXTRA_KEY_IN_LOCATION, unzipLocation);
        startService(intentMyIntentService);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == REQUEST_CODE) {
            if ((data != null) && (data.getBooleanExtra("isCheckNewVersion", false))) {
                checkUrlVersion();
            } else {
                startGameActivity();
            }
        } else startGameActivity();
    }

    private void startDeletion(boolean isAll) {
        if (isAll) {
            new DeleteTask().execute(unzipLocation);
        } else {
            new DeleteTask().execute(unzipLocation + "builtin", unzipLocation + "games", unzipLocation + "debug.txt");
        }
    }

    private void checkAppVersion() {
        if (!isRestored() && getBuildNumber().equals(getString(R.string.ver))) {
            addImageView(1);
            runGame();
        } else if (getBuildNumber().equals("0")) {
            addImageView(0);
            saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
            startDeletion(true);
        } else {
            addImageView(0);
            saveSettings(TAG_BUILD_NUMBER, getString(R.string.ver));
            startDeletion(false);
        }
    }

    private class DeleteTask extends AsyncTask<String, Void, Void> {
        String location;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBarIndeterminate.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.VISIBLE);
            mLoading.setText(R.string.rm_old);
        }

        @Override
        protected Void doInBackground(String... params) {
            location = params[0];
            for (String p : params) {
                deleteFiles(p);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            if (isFinishing())
                return;
            if (unzipLocation.equals(location)) {
                new CopyZip().execute(FILES, WORLDS, GAMES);
            } else {
                new CopyZip().execute(FILES, GAMES);
            }
        }


    }

    private class CopyZip extends AsyncTask<String, Void, String> {
        String[] zips;

        @Override
        protected String doInBackground(String... params) {
            zips = params;
            for (String s : zips) {
                copyAssets(s);
            }
            return "Done";

        }


        @Override
        protected void onPostExecute(String result) {
            mLoading.setText(R.string.loading);
            mProgressBarIndeterminate.setVisibility(View.GONE);
            try {
                startUnzipService(zips);
            } catch (IOException e) {
                Log.e(TAG, "unzip failed: " + e.getMessage());
            }
        }

        private void copyAssets(String zipName) {
            String filename = zipName.substring(zipName.lastIndexOf("/") + 1);
            InputStream in;
            OutputStream out;
            try {
                in = getAssets().open(filename);
                out = new FileOutputStream(zipName);
                copyFile(in, out);
                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy asset file: " + e.getMessage());
            }
        }

        private void copyFile(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }


}