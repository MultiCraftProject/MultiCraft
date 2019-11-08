package com.multicraft.game;

import androidx.multidex.MultiDexApplication;

import com.bugsnag.android.Bugsnag;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Bugsnag.init(this);
    }
}
