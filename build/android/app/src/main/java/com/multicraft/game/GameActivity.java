package com.multicraft.game;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.bugsnag.android.Bugsnag;

import static com.multicraft.game.PreferencesHelper.TAG_BUILD_NUMBER;
import static com.multicraft.game.PreferencesHelper.getInstance;

/*import static com.multicraft.game.AdManager.initAd;
import static com.multicraft.game.AdManager.setAdsCallback;
import static com.multicraft.game.AdManager.startAd;
import static com.multicraft.game.AdManager.stopAd;*/

public class GameActivity extends NativeActivity {
    static {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("MultiCraft");
        } catch (UnsatisfiedLinkError e) {
            Bugsnag.notify(e);
        } catch (IllegalArgumentException e) {
            Bugsnag.notify(e);
        } catch (OutOfMemoryError e) {
            Bugsnag.notify(e);
        } catch (Error | Exception error) {
            Bugsnag.notify(error);
        }
    }

    private int messageReturnCode;
    private String messageReturnValue;
    private int height, width;

    public static native void putMessageBoxResult(String text);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        height = bundle != null ? bundle.getInt("height", 0) : getResources().getDisplayMetrics().heightPixels;
        width = bundle != null ? bundle.getInt("width", 0) : getResources().getDisplayMetrics().widthPixels;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        messageReturnCode = -1;
        messageReturnValue = "";
//        new AdInitTask().execute();
    }

    private void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19)
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            makeFullScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    @Override
    public void onBackPressed() {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                String text = data.getStringExtra("text");
                messageReturnCode = 0;
                messageReturnValue = text;
            } else
                messageReturnCode = 1;
        }
    }

    public void showDialog(String acceptButton, String hint, String current, int editType) {
        Intent intent = new Intent(this, InputDialogActivity.class);
        Bundle params = new Bundle();
        params.putString("acceptButton", acceptButton);
        params.putString("hint", hint);
        params.putString("current", current);
        params.putInt("editType", editType);
        intent.putExtras(params);
        startActivityForResult(intent, 101);
        messageReturnValue = "";
        messageReturnCode = -1;
    }

    public int getDialogState() {
        return messageReturnCode;
    }

    public String getDialogValue() {
        messageReturnCode = -1;
        return messageReturnValue;
    }

    public float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    public int getDisplayHeight() {
        return height;
    }

    public int getDisplayWidth() {
        return width;
    }

    public float getMemoryMax() {
        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            double memory = memInfo.totalMem * 1.0f / (1024 * 1024 * 1024);
            return (Math.round(memory * 100) / 100.0f);
        } else {
            Bugsnag.leaveBreadcrumb("RAM: Cannot get RAM");
            return 1;
        }
    }

    public void notifyServerConnect(boolean isMultiPlayer) {
         /*isMultiPlayer = isMultiPlayer;
        if (isMultiPlayer) stopAd();*/
    }

    public void notifyAbortLoading() {
        PreferencesHelper pf = getInstance(this);
        pf.saveSettings(TAG_BUILD_NUMBER, "1");
    }

    public void notifyExitGame() {
        /*if (isMultiPlayer)
            startAd(this, false, true);*/
    }

    @SuppressLint("StaticFieldLeak")
    class AdInitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            /*initAd(GameActivity.this, consent);*/
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /*setAdsCallback(GameActivity.this);*/
        }
    }
}
