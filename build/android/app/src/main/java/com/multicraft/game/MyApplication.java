package com.multicraft.game;

import android.app.Application;
/*import androidx.multidex.MultiDexApplication;*/

import com.bugsnag.android.Bugsnag;

public class MyApplication extends Application {
/*public class MyApplication extends MultiDexApplication {*/
    @Override
    public void onCreate() {
        super.onCreate();
        Bugsnag.init(this);
    }
}
