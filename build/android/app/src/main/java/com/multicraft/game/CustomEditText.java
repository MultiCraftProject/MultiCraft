package com.multicraft.game;

import android.content.Context;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText {

    public CustomEditText(Context context) {
        super(context);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            InputMethodManager mgr = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(mgr).hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
        return false;
    }
}
