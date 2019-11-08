package com.multicraft.game;

import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


class AlertDialogHelper {
    private final AppCompatActivity activity;
    private DialogsCallback sCallback = null;
    private Drawable icon = null;
    private String title = null;
    private CharSequence message = null;
    private String buttonPositive = null;
    private String buttonNegative = null;
    private String buttonNeutral = null;

    AlertDialogHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    private Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    private String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private CharSequence getMessage() {
        return message;
    }

    void setMessage(CharSequence message) {
        this.message = message;
    }

    private String getButtonPositive() {
        return buttonPositive;
    }

    void setButtonPositive(String buttonPositive) {
        this.buttonPositive = buttonPositive;
    }

    private String getButtonNegative() {
        return buttonNegative;
    }

    void setButtonNegative(String buttonNegative) {
        this.buttonNegative = buttonNegative;
    }

    private String getButtonNeutral() {
        return buttonNeutral;
    }

    void setButtonNeutral(String buttonNeutral) {
        this.buttonNeutral = buttonNeutral;
    }

    void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    void showAlert(final String source) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (getIcon() != null) builder.setIcon(getIcon());
        if (getTitle() != null) builder.setTitle(getTitle());
        if (getMessage() != null) builder.setMessage(getMessage());
        if (getButtonPositive() != null)
            builder.setPositiveButton(getButtonPositive(), (dialogInterface, i) -> sCallback.onPositive(source));
        if (getButtonNegative() != null)
            builder.setNegativeButton(getButtonNegative(), (dialogInterface, i) -> sCallback.onNegative(source));
        if (getButtonNeutral() != null)
            builder.setNeutralButton(getButtonNeutral(), (dialogInterface, i) -> sCallback.onNeutral(source));
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        if (!activity.isFinishing())
            dialog.show();
    }
}
