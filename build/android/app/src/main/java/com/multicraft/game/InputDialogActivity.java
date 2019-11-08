package com.multicraft.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class InputDialogActivity extends AppCompatActivity {
    private AlertDialog alertDialog;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        int editType = Objects.requireNonNull(b).getInt("editType");
        String hint = b.getString("hint");
        String current = b.getString("current");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText editText = new EditText(this);
        builder.setView(editText);
        editText.requestFocus();
        editText.setHint(hint);
        editText.setText(current);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        if (editType == 3) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        editText.setOnKeyListener((view, KeyCode, event) -> {
            if (KeyCode == KeyEvent.KEYCODE_ENTER) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                pushResult(editText.getText().toString());
                return true;
            }
            return false;
        });
        alertDialog = builder.create();
        if (!this.isFinishing())
            alertDialog.show();
        alertDialog.setOnCancelListener(dialog -> {
            pushResult(editText.getText().toString());
            setResult(Activity.RESULT_CANCELED);
            alertDialog.dismiss();
            makeFullScreen();
            finish();
        });
    }

    private void pushResult(String text) {
        Intent resultData = new Intent();
        resultData.putExtra("text", text);
        setResult(AppCompatActivity.RESULT_OK, resultData);
        alertDialog.dismiss();
        makeFullScreen();
        finish();
    }

    private void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19)
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
