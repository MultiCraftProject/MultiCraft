/*
MultiCraft
Copyright (C) 2014-2020 MoNTE48, Maksim Gamarnik <MoNTE48@mail.ua>
Copyright (C) 2014-2020 ubulem,  Bektur Mambetov <berkut87@gmail.com>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 3.0 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.multicraft.game.helpers;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.multicraft.game.callbacks.DialogsCallback;


public class AlertDialogHelper {
    private final AppCompatActivity activity;
    private DialogsCallback sCallback = null;
    private Drawable icon = null;
    private String title = null;
    private CharSequence message = null;
    private TextView tv = null;
    private String buttonPositive = null;
    private String buttonNegative = null;
    private String buttonNeutral = null;

    public AlertDialogHelper(AppCompatActivity activity) {
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

    public void setMessage(CharSequence message) {
        this.message = message;
    }

    private TextView getTV() {
        return tv;
    }

    public void setTV(TextView tv) {
        this.tv = tv;
    }

    private String getButtonPositive() {
        return buttonPositive;
    }

    public void setButtonPositive(String buttonPositive) {
        this.buttonPositive = buttonPositive;
    }

    private String getButtonNegative() {
        return buttonNegative;
    }

    public void setButtonNegative(String buttonNegative) {
        this.buttonNegative = buttonNegative;
    }

    private String getButtonNeutral() {
        return buttonNeutral;
    }

    public void setButtonNeutral(String buttonNeutral) {
        this.buttonNeutral = buttonNeutral;
    }

    public void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    public void showAlert(final String source) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (getIcon() != null) builder.setIcon(getIcon());
        if (getTitle() != null) builder.setTitle(getTitle());
        if (getMessage() != null) builder.setMessage(getMessage());
        if (getTV() != null) builder.setView(getTV());
        if (getButtonPositive() != null)
            builder.setPositiveButton(getButtonPositive(), (dialogInterface, i) -> {
                dialogInterface.dismiss();
                sCallback.onPositive(source);
            });
        if (getButtonNegative() != null)
            builder.setNegativeButton(getButtonNegative(), (dialogInterface, i) -> {
                dialogInterface.dismiss();
                sCallback.onNegative(source);
            });
        if (getButtonNeutral() != null)
            builder.setNeutralButton(getButtonNeutral(), (dialogInterface, i) -> {
                dialogInterface.dismiss();
                sCallback.onNeutral(source);
            });
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        if (!activity.isFinishing())
            dialog.show();
    }
}
