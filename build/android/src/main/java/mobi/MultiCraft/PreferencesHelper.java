package mobi.MultiCraft;

import android.content.Context;
import android.content.SharedPreferences;

class PreferencesHelper {
    static final String TAG_SHORTCUT_CREATED = "createShortcut";
    static final String TAG_BUILD_NUMBER = "buildNumber";
    static final String TAG_LAUNCH_TIMES = "launchTimes";
    static final String TAG_RESTORE_BACKUP = "restoredFromBackup";
    static final String TAG_CONSENT_ASKED = "consentAsked";
    private static final String SETTINGS = "settings";
    private static String buildNumber;
    private static boolean createShortcut;
    private static boolean askConsent;

    private static SharedPreferences settings;
    private static boolean disabledADS;

    static boolean isCreateShortcut() {
        return createShortcut;
    }

    static boolean isAskConsent() {
        return askConsent;
    }

    static String getBuildNumber() {
        return buildNumber;
    }

    static boolean isRestored() {
        return settings.getBoolean(TAG_RESTORE_BACKUP, false);
    }

    static int getLaunchTimes() {
        return settings.getInt(TAG_LAUNCH_TIMES, 0);
    }

    static void loadSettings(final Context context) {
        settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        createShortcut = settings.getBoolean(TAG_SHORTCUT_CREATED, true);
        askConsent = settings.getBoolean(TAG_CONSENT_ASKED, true);
        buildNumber = settings.getString(TAG_BUILD_NUMBER, "0");
    }

    static void saveSettings(String tag, boolean bool) {
        settings.edit().putBoolean(tag, bool).apply();
    }

    static void saveSettings(String tag, String value) {
        settings.edit().putString(tag, value).apply();
    }

    static void saveSettings(String tag, int value) {
        settings.edit().putInt(tag, value).apply();
    }

}