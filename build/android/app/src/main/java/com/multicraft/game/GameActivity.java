package com.multicraft.game;

import android.app.ActivityManager;
import android.app.NativeActivity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.bugsnag.android.Bugsnag;
import com.multicraft.game.helpers.PreferencesHelper;

import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.multicraft.game.AdManager.initAd;
import static com.multicraft.game.AdManager.setAdsCallback;
import static com.multicraft.game.AdManager.startAd;
import static com.multicraft.game.AdManager.stopAd;
import static com.multicraft.game.helpers.PreferencesHelper.TAG_BUILD_NUMBER;
import static com.multicraft.game.helpers.PreferencesHelper.getInstance;
import static com.multicraft.game.helpers.Utilities.makeFullScreen;

public class GameActivity extends NativeActivity {
    static {
        try {
            System.loadLibrary("MultiCraft");
        } catch (UnsatisfiedLinkError | OutOfMemoryError e) {
            Bugsnag.notify(e);
            System.exit(0);
        } catch (IllegalArgumentException i) {
            Bugsnag.notify(i);
            System.exit(0);
        } catch (Error | Exception error) {
            Bugsnag.notify(error);
            System.exit(0);
        }
    }

    private int messageReturnCode = -1;
    private String messageReturnValue = "";
    private int height, width;
    private boolean consent, isMultiPlayer;
    private PreferencesHelper pf;
    private Disposable adInitSub;

    public static native void putMessageBoxResult(String text);

    public static native void pauseGame();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        height = bundle != null ? bundle.getInt("height", 0) : getResources().getDisplayMetrics().heightPixels;
        width = bundle != null ? bundle.getInt("width", 0) : getResources().getDisplayMetrics().widthPixels;
        consent = bundle == null || bundle.getBoolean("consent", true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pf = getInstance(this);
        if (pf.isAdsEnable()) {
            adInitSub = Completable.fromAction(() -> initAd(this, consent))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> setAdsCallback(this));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            makeFullScreen(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFullScreen(this);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adInitSub.dispose();
    }

    public void showDialog(String acceptButton, String hint, String current, int editType) {
        runOnUiThread(() -> showDialogUI(hint, current, editType));
    }

    private void showDialogUI(String hint, String current, int editType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editText = new CustomEditText(this);
        builder.setView(editText);
        AlertDialog alertDialog = builder.create();
        editText.requestFocus();
        editText.setHint(hint);
        editText.setText(current);
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
        if (editType == 1)
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        else if (editType == 3)
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        else
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setSelection(editText.getText().length());
        editText.setOnKeyListener((view, KeyCode, event) -> {
            if (KeyCode == KeyEvent.KEYCODE_ENTER) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                messageReturnCode = 0;
                messageReturnValue = editText.getText().toString();
                alertDialog.dismiss();
                return true;
            }
            return false;
        });
        alertDialog.show();
        alertDialog.setOnCancelListener(dialog -> {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            messageReturnValue = current;
            messageReturnCode = -1;
        });
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
        float memory = 1.0f;
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
            memory = memInfo.totalMem * 1.0f / (1024 * 1024 * 1024);
            memory = Math.round(memory * 100) / 100.0f;
        }
        return memory;
    }

    public void notifyServerConnect(boolean multiplayer) {
        isMultiPlayer = multiplayer;
        if (isMultiPlayer)
            stopAd();
    }

    public void notifyAbortLoading() {
        pf.saveSettings(TAG_BUILD_NUMBER, "1");
    }

    public void notifyExitGame() {
        if (isMultiPlayer && pf.isAdsEnable())
            startAd(this, false, true);
    }
}
