package mobi.MultiCraft;

import android.content.Context;
import android.content.SharedPreferences;

class PreferencesHelper {
    static final String TAG_SHORTCUT_CREATED = "createShortcut";
    static final String TAG_BUILD_NUMBER = "buildNumber";
    static final String TAG_LAUNCH_TIMES = "launchTimes";
    static final String TAG_CONSENT_ASKED = "consentAsked";
    static final String TAG_RESTORE_BACKUP = "restoredFromBackup";
    static final String IS_LOADED = "interstitialLoaded";
    static final String RV_LOADED = "rewardedVideoLoaded";
    private static final String SETTINGS = "MultiCraftSettings";
    private static final String TAG_DISABLED_ADS = "disabledADS";

    private static PreferencesHelper instance;
    private static SharedPreferences sharedPreferences;


    private PreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    }

    static PreferencesHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (PreferencesHelper.class) {
                if (instance == null) {
                    instance = new PreferencesHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    boolean isCreateShortcut() {
        return sharedPreferences.getBoolean(TAG_SHORTCUT_CREATED, true);
    }

    boolean isInterstitialLoaded() {
        return sharedPreferences.getBoolean(IS_LOADED, false);
    }

    boolean isVideoLoaded() {
        return sharedPreferences.getBoolean(RV_LOADED, false);
    }

    boolean isAskConsent() {
        return sharedPreferences.getBoolean(TAG_CONSENT_ASKED, true);
    }

    boolean isRestored() {
        return sharedPreferences.getBoolean(TAG_RESTORE_BACKUP, false);
    }

    boolean isAdsEnabled() {
        return !sharedPreferences.getBoolean(TAG_DISABLED_ADS, false);
    }

    String getBuildNumber() {
        return sharedPreferences.getString(TAG_BUILD_NUMBER, "0");
    }

    void savePurchase(boolean v) {
        sharedPreferences.edit().putBoolean(TAG_DISABLED_ADS, v).apply();
    }

    int getLaunchTimes() {
        return sharedPreferences.getInt(TAG_LAUNCH_TIMES, 0);
    }

    void saveSettings(String tag, boolean bool) {
        sharedPreferences.edit().putBoolean(tag, bool).apply();
    }

    void saveSettings(String tag, String value) {
        sharedPreferences.edit().putString(tag, value).apply();
    }

    void saveSettings(String tag, int value) {
        sharedPreferences.edit().putInt(tag, value).apply();
    }

}