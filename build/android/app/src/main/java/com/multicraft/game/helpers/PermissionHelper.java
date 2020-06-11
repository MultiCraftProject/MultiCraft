package com.multicraft.game.helpers;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import com.multicraft.game.R;
import com.multicraft.game.callbacks.CallBackListener;
import com.tedpark.tedpermission.rx2.TedRx2Permission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class PermissionHelper {
    private final AppCompatActivity activity;
    private CallBackListener listener;
    private PreferencesHelper pf;

    public PermissionHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setListener(CallBackListener listener) {
        this.listener = listener;
    }

    public void askPermissions() {
        pf = PreferencesHelper.getInstance(activity);
        askStoragePermissions();
    }

    // permission block
    @SuppressLint("CheckResult")
    private void askStoragePermissions() {
        TedRx2Permission.with(activity)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                        if (pf.getLaunchTimes() % 3 == 1)
                            askLocationPermissions();
                        else listener.onEvent(true);
                    } else {
                        if (TedRx2Permission.canRequestPermission(activity, WRITE_EXTERNAL_STORAGE))
                            askStorageRationalePermissions();
                        else askStorageWhenDoNotShow();
                    }
                });
    }

    // storage permissions block
    @SuppressLint("CheckResult")
    private void askStorageRationalePermissions() {
        TedRx2Permission.with(activity)
                .setRationaleMessage(R.string.explain)
                .setDeniedMessage(R.string.denied)
                .setDeniedCloseButtonText(R.string.close_game)
                .setGotoSettingButtonText(R.string.settings)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                        if (pf.getLaunchTimes() % 3 == 1)
                            askLocationPermissions();
                        else listener.onEvent(true);
                    } else {
                        listener.onEvent(false);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void askStorageWhenDoNotShow() {
        TedRx2Permission.with(activity)
                .setDeniedMessage(R.string.denied)
                .setDeniedCloseButtonText(R.string.close_game)
                .setGotoSettingButtonText(R.string.settings)
                .setPermissions(WRITE_EXTERNAL_STORAGE)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                        if (pf.getLaunchTimes() % 3 == 1)
                            askLocationPermissions();
                        else listener.onEvent(true);
                    } else {
                        listener.onEvent(false);
                    }
                });
    }

    // location permissions block
    @SuppressLint("CheckResult")
    private void askLocationPermissions() {
        TedRx2Permission.with(activity)
                .setPermissions(ACCESS_COARSE_LOCATION)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                        listener.onEvent(true);
                    } else {
                        if (TedRx2Permission.canRequestPermission(activity, ACCESS_COARSE_LOCATION))
                            askLocationRationalePermissions();
                        else listener.onEvent(true);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void askLocationRationalePermissions() {
        TedRx2Permission.with(activity)
                .setRationaleMessage(R.string.location)
                .setPermissions(ACCESS_COARSE_LOCATION)
                .request()
                .subscribe(tedPermissionResult -> listener.onEvent(true));
    }
}
