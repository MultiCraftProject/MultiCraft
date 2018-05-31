package mobi.MultiCraft;

import android.app.NativeActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class GameActivity extends NativeActivity {
    static {
        System.loadLibrary("multicraft");
    }

    private int messageReturnCode;
    private String messageReturnValue;
    private int height, width;
    private float density;

    public static native void putMessageBoxResult(String text);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        density = bundle != null ? bundle.getFloat("density", 0) : getResources().getDisplayMetrics().density;
        height = bundle != null ? bundle.getInt("height", 0) : getResources().getDisplayMetrics().heightPixels;
        width = bundle != null ? bundle.getInt("width", 0) : getResources().getDisplayMetrics().widthPixels;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        if (!isAdsDisabled())
//            startAd(this, true);
        messageReturnCode = -1;
        messageReturnValue = "";
        makeFullScreen();
    }


    public void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            makeFullScreen();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        if (!isAdsDisabled())
//            startAd(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (!isAdsDisabled())
//            stopAd();
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
            } else {
                messageReturnCode = 1;
            }
        }
    }

    public void copyAssets() {
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
        return density;
    }

    public int getDisplayHeight() {
        return height;
    }

    public int getDisplayWidth() {
        return width;
    }

}