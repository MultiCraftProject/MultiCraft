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

package com.multicraft.game;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bugsnag.android.Bugsnag;
import com.multicraft.game.callbacks.DialogsCallback;
import com.multicraft.game.helpers.PreferencesHelper;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Objects;

import static com.multicraft.game.helpers.ApiLevelHelper.isGreaterOrEqualKitkat;

class RateMe {
    private static final int INSTALL_DAYS = 5;
    private static final int LAUNCH_TIMES = 4;
    private static final String PREF_NAME = "RateMe";
    private static final String KEY_INSTALL_DATE = "rta_install_date";
    private static final String KEY_OPT_OUT = "rta_opt_out";
    private static Date mInstallDate = new Date();
    private static boolean mOptOut = false;
    private static DialogsCallback sCallback = null;

    private static WeakReference<AppCompatActivity> activityRef = null;

    static void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    static void onStart(AppCompatActivity activity) {
        activityRef = new WeakReference<>(activity);
        SharedPreferences pref = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L)
            storeInstallDate(activity, editor);
        editor.apply();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
    }

    static boolean shouldShowRateDialog() {
        if (mOptOut)
            return false;
        else {
            if (PreferencesHelper.getInstance(activityRef.get()).getLaunchTimes() % LAUNCH_TIMES == 0)
                return true;
            long threshold = INSTALL_DAYS * 24 * 60 * 60 * 1000L;
            return new Date().getTime() - mInstallDate.getTime() >= threshold;
        }
    }

    static void showRateDialog() {
        final AppCompatActivity activity = activityRef.get();
        final Dialog dialog = new Dialog(activity, R.style.RateMe);
        dialog.setCancelable(false);
        if (isGreaterOrEqualKitkat())
            Objects.requireNonNull(dialog.getWindow()).getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        dialog.setContentView(R.layout.rate_dialog);
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (rating >= 4) {
                sCallback.onPositive("RateMe");
                dialog.dismiss();
                setOptOut(activity);
            } else {
                sCallback.onNegative("RateMe");
                dialog.dismiss();
                Toast.makeText(activity, R.string.sad, Toast.LENGTH_LONG).show();
                clearSharedPreferences(activity);
            }
        });
        dialog.setOnCancelListener(dialog1 -> {
            sCallback.onNeutral("RateMe");
            clearSharedPreferences(activity);
        });
        if (!activity.isFinishing())
            dialog.show();
        else
            sCallback.onNegative("RateMe");
    }

    private static void clearSharedPreferences(AppCompatActivity activity) {
        Editor editor = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(KEY_INSTALL_DATE);
        editor.apply();
    }

    private static void setOptOut(final AppCompatActivity activity) {
        Editor editor = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_OPT_OUT, true);
        editor.apply();
    }

    private static void storeInstallDate(final AppCompatActivity activity, Editor editor) {
        Date installDate = new Date();
        PackageManager packageManager = activity.getPackageManager();
        try {
            PackageInfo pkgInfo = packageManager.getPackageInfo(activity.getPackageName(), 0);
            installDate = new Date(pkgInfo.firstInstallTime);
        } catch (PackageManager.NameNotFoundException e) {
            Bugsnag.notify(e);
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.getTime());
    }
}
