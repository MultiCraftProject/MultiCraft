package com.multicraft.game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

import static com.multicraft.game.MainActivity.UPDATE_LINK;

class WVersionManager implements DialogsCallback {
    private final CustomTagHandler customTagHandler;
    private final String PREF_IGNORE_VERSION_CODE = "w.ignore.version.code";
    private final String PREF_REMINDER_TIME = "w.reminder.time";
    private final String PREF_LAUNCH_TIMES = "w.launch.times";
    private final AppCompatActivity activity;
    private final ActivityListener al;
    private DialogsCallback sCallback = null;
    private String message;
    private String updateUrl;
    private int mVersionCode;

    WVersionManager(AppCompatActivity act) {
        this.activity = act;
        al = (ActivityListener) act;
        this.customTagHandler = new CustomTagHandler();
        setLaunchTimes();
    }

    void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    void checkVersion() {
        Calendar c = Calendar.getInstance();
        long currentTimeStamp = c.getTimeInMillis();
        long reminderTimeStamp = getReminderTime();
        if (currentTimeStamp > reminderTimeStamp) {
            // fire request to get update version content
            VersionContentRequest request = new VersionContentRequest();
            request.execute(UPDATE_LINK);
        } else
            al.isShowUpdateDialog(false);
    }

    void showDialog() {
        AlertDialogHelper dialogHelper = new AlertDialogHelper(activity);
        dialogHelper.setListener(this);
        dialogHelper.setIcon(activity.getResources().getDrawable(R.mipmap.ic_launcher));
        dialogHelper.setTitle(activity.getString(R.string.available));
        dialogHelper.setMessage(Html.fromHtml(getMessage(), null, getCustomTagHandler()));
        dialogHelper.setButtonPositive(activity.getString(R.string.update));
        dialogHelper.setButtonNeutral(activity.getString(R.string.later));
        dialogHelper.showAlert("WVersionManager");
    }

    private void setLaunchTimes() {
        int launchTimes = getLaunchTimes();
        launchTimes++;
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(PREF_LAUNCH_TIMES, launchTimes)
                .apply();
    }

    private String getMessage() {
        String defaultMessage = "What's new in this version";
        return message != null ? message : defaultMessage;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    String getUpdateUrl() {
        return updateUrl != null ? updateUrl : getGooglePlayStoreUrl();
    }

    private void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    void updateNow(String url) {
        if (url != null) {
            try {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            } catch (Exception e) {
                Bugsnag.notify(e);
            }
        }

    }

    void remindMeLater() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        long reminderTimeStamp = c.getTimeInMillis();
        setReminderTime(reminderTimeStamp);
    }

    private long getReminderTime() {
        return PreferenceManager.getDefaultSharedPreferences(activity).getLong(PREF_REMINDER_TIME, 0);
    }

    private void setReminderTime(long reminderTimeStamp) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(PREF_REMINDER_TIME, reminderTimeStamp)
                .apply();
    }

    void ignoreThisVersion() {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(PREF_IGNORE_VERSION_CODE, mVersionCode)
                .apply();
    }

    private String getGooglePlayStoreUrl() {
        String id = activity.getApplicationInfo().packageName; // current google play is using package name as id
        return "market://details?id=" + id;
    }

    private int getLaunchTimes() {
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(PREF_LAUNCH_TIMES, 0);
    }

    private int getCurrentVersionCode() {
        int currentVersionCode = 0;
        PackageInfo pInfo;
        try {
            pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            currentVersionCode = pInfo.versionCode;
        } catch (NameNotFoundException e) {
            // return 0
        }
        return currentVersionCode;
    }

    private int getIgnoreVersionCode() {
        return PreferenceManager.getDefaultSharedPreferences(activity).getInt(PREF_IGNORE_VERSION_CODE, 1);
    }

    private CustomTagHandler getCustomTagHandler() {
        return customTagHandler;
    }

    @Override
    public void onPositive(String source) {
        sCallback.onPositive("WVersionManager");
    }

    @Override
    public void onNegative(String source) {
        sCallback.onNegative("WVersionManager");
    }

    @Override
    public void onNeutral(String source) {
        sCallback.onNeutral("WVersionManager");
    }

    interface ActivityListener {
        void isShowUpdateDialog(boolean flag);
    }

    private static class CustomTagHandler implements Html.TagHandler {
        @Override
        public void handleTag(boolean opening, String tag, Editable output,
                              XMLReader xmlReader) {
            // you may add more tag handler which are not supported by android here
            if ("li".equals(tag)) {
                if (opening)
                    output.append(" \u2022 ");
                else
                    output.append("\n");
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class VersionContentRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... uri) {
            String result = null;
            try {
                URL u = new URL(UPDATE_LINK);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(5000);
                c.connect();
                InputStream in = c.getInputStream();
                final ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                in.read(buffer); // Read from Buffer.
                bo.write(buffer); // Write Into Buffer.
                result = bo.toString();
                bo.close();
            } catch (MalformedURLException e) {
                Bugsnag.notify(e);
            } catch (ProtocolException e) {
                Bugsnag.notify(e);
            } catch (IOException e) {
                // nothing
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            PreferencesHelper pf = PreferencesHelper.getInstance(activity);
            mVersionCode = 0;
            if (result != null) {
                try {
                    String content;
                    if (!result.startsWith("{")) { // for response who append with unknown char
                        result = result.substring(1);
                    }
                    String mResult = result;
                    // json format from server:
                    JSONObject json = (JSONObject) new JSONTokener(mResult).nextValue();
                    mVersionCode = json.optInt("version_code");
                    String lang = Locale.getDefault().getLanguage();
                    if (lang.equals("ru"))
                        content = json.optString("content_ru");
                    else
                        content = json.optString("content_en");
                    String packageName = json.optString("package");
                    setUpdateUrl("market://details?id=" + packageName);
                    int adsDelay = json.optInt("ads_delay");
                    int adsRepeat = json.optInt("ads_repeat");
                    pf.saveSettings(PreferencesHelper.ADS_DELAY, adsDelay);
                    pf.saveSettings(PreferencesHelper.ADS_REPEAT, adsRepeat);
                    int currentVersionCode = getCurrentVersionCode();
                    if (currentVersionCode < mVersionCode) {
                        if (mVersionCode != getIgnoreVersionCode()) {
                            setMessage(content);
                            al.isShowUpdateDialog(true);
                        } else if (mVersionCode == getIgnoreVersionCode() && getLaunchTimes() % 3 == 0) {
                            PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(PREF_LAUNCH_TIMES, 0)
                                    .apply();
                            setMessage(content);
                            al.isShowUpdateDialog(true);
                        } else
                            al.isShowUpdateDialog(false);
                    } else
                        al.isShowUpdateDialog(false);
                } catch (JSONException e) {
                    Bugsnag.notify(e);
                    al.isShowUpdateDialog(false);
                } catch (Exception e) {
                    Bugsnag.notify(e);
                    al.isShowUpdateDialog(false);
                }
            } else
                al.isShowUpdateDialog(false);
        }
    }
}
