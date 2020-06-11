package com.multicraft.game.helpers;

import android.os.Build;

public class ApiLevelHelper {
    public static boolean isGreaterOrEqual(int versionCode) {
        return Build.VERSION.SDK_INT >= versionCode;
    }

    public static boolean isGreaterOrEqualKitkat() {
        return isGreaterOrEqual(Build.VERSION_CODES.KITKAT);
    }

    public static boolean isGreaterOrEqualLollipop() {
        return isGreaterOrEqual(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean isGreaterOrEqualOreo() {
        return isGreaterOrEqual(Build.VERSION_CODES.O);
    }

    public static boolean isGreaterOrEqualQ() {
        return isGreaterOrEqual(Build.VERSION_CODES.Q);
    }
}
