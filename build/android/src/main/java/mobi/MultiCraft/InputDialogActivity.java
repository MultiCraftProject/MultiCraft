package mobi.MultiCraft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class InputDialogActivity extends Activity {
    private AlertDialog alertDialog;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        int editType = b.getInt("editType");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.input_dialog, null);
        builder.setView(dialogView);
        final EditText editText = dialogView.findViewById(R.id.editText);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        if (editType == 3) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int KeyCode, KeyEvent event) {
                if (KeyCode == KeyEvent.KEYCODE_ENTER) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    pushResult(editText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                pushResult(editText.getText().toString());
                setResult(Activity.RESULT_CANCELED);
                alertDialog.dismiss();
                makeFullScreen();
                finish();
            }
        });
    }

    public void pushResult(String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && text.matches(".*[А-я].*")) {
            text = Transliteration.toLatin(text);
        }
        Intent resultData = new Intent();
        resultData.putExtra("text", text);
        setResult(Activity.RESULT_OK, resultData);
        alertDialog.dismiss();
        makeFullScreen();
        finish();
    }

    public void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}