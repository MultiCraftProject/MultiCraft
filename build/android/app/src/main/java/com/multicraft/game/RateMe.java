package com.multicraft.game;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Objects;

class RateMe {
    private static final int INSTALL_DAYS = 5;
    private static final int LAUNCH_TIMES = 3;
    private static final String GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=";
    private static final String PREF_NAME = "RateMe";
    private static final String KEY_INSTALL_DATE = "rta_install_date";
    private static final String KEY_LAUNCH_TIMES = "rta_launch_times";
    private static final String KEY_OPT_OUT = "rta_opt_out";
    private static Date mInstallDate = new Date();
    private static int mLaunchTimes = 0;
    private static boolean mOptOut = false;
    private static DialogsCallback sCallback = null;

    private static WeakReference<AppCompatActivity> mainActivityRef = null;

    static void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    static void onStart(Context context) {
        mainActivityRef = new WeakReference<>((AppCompatActivity) context);
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L)
            storeInstallDate(context, editor);
        // Increment launch times
        int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        launchTimes++;
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes);

        editor.apply();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
    }

    static boolean shouldShowRateDialog() {
        if (mOptOut)
            return false;
        else {
            if (mLaunchTimes >= LAUNCH_TIMES)
                return true;
            long threshold = INSTALL_DAYS * 24 * 60 * 60 * 1000L;
            return new Date().getTime() - mInstallDate.getTime() >= threshold;
        }
    }

    static void showRateDialog() {
        final AppCompatActivity activity = mainActivityRef.get();
        final Dialog dialog = new Dialog(activity, R.style.RateMe);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT >= 19)
            Objects.requireNonNull(dialog.getWindow()).getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        dialog.setContentView(R.layout.rate_dialog);
        dialog.setTitle(R.string.rta_dialog_title);

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (rating >= 4) {
                if (sCallback != null)
                    sCallback.onPositive("RateMe");
                dialog.dismiss();
                String appPackage = activity.getPackageName();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY + appPackage));
                activity.startActivity(intent);
                setOptOut(activity);
            } else {
                if (sCallback != null)
                    sCallback.onNegative("RateMe");
                dialog.dismiss();
                clearSharedPreferences(activity);
            }
        });
        dialog.setOnCancelListener(dialog1 -> {
            if (sCallback != null)
                sCallback.onNeutral("RateMe");
            clearSharedPreferences(activity);
        });
        if (!activity.isFinishing())
            dialog.show();
        else if (sCallback != null)
            sCallback.onNegative("RateMe");
    }

    private static void clearSharedPreferences(AppCompatActivity context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.remove(KEY_INSTALL_DATE);
        editor.remove(KEY_LAUNCH_TIMES);
        editor.apply();
    }

    private static void setOptOut(final AppCompatActivity context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putBoolean(KEY_OPT_OUT, true);
        editor.apply();
        mOptOut = true;
    }

    private static void storeInstallDate(final Context context, SharedPreferences.Editor editor) {
        Date installDate = new Date();
        PackageManager packMan = context.getPackageManager();
        try {
            PackageInfo pkgInfo = packMan.getPackageInfo(context.getPackageName(), 0);
            installDate = new Date(pkgInfo.firstInstallTime);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.getTime());
    }
}
