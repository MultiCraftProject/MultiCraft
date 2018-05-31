package mobi.MultiCraft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;

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

class WVersionManager {
    private static final String TAG = "WVersionManager";
    private DialogsCallback sCallback = null;
    private CustomTagHandler customTagHandler;
    private String PREF_IGNORE_VERSION_CODE = "w.ignore.version.code";
    private String PREF_REMINDER_TIME = "w.reminder.time";
    private String PREF_LAUNCH_TIMES = "w.launch.times";
    private Activity activity;
    private Drawable icon;
    private String title;
    private String message;
    private String updateNowLabel;
    private String remindMeLaterLabel;
    private String ignoreThisVersionLabel;
    private String updateUrl;
    private String versionContentUrl;
    private int reminderTimer;
    private int mVersionCode;
    private AlertDialogButtonListener listener;
    private boolean mDialogCancelable = false;
    private ActivityListener al;

    WVersionManager(Activity act) {
        this.activity = act;
        al = (ActivityListener) act;
        this.listener = new AlertDialogButtonListener();
        this.customTagHandler = new CustomTagHandler();
        setLaunchTimes();
    }

    void setListener(DialogsCallback callback) {
        sCallback = callback;
    }

    private Drawable getDefaultAppIcon() {
        return activity.getApplicationInfo().loadIcon(activity.getPackageManager());
    }

    void checkVersion() {
        String versionContentUrl = getVersionContentUrl();
        if (versionContentUrl == null) {
            Log.e(TAG, "Please set versionContentUrl first");
            return;
        }

        Calendar c = Calendar.getInstance();
        long currentTimeStamp = c.getTimeInMillis();
        long reminderTimeStamp = getReminderTime();
        if (currentTimeStamp > reminderTimeStamp) {
            // fire request to get update version content
            VersionContentRequest request = new VersionContentRequest(activity);
            request.execute(getVersionContentUrl());
        } else {
            al.isShowUpdateDialog(false);
        }
    }

    void showDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(activity, R.style.CustomLollipopDialogStyle);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
        builder.setIcon(getIcon());
        builder.setTitle(getTitle());
        builder.setMessage(Html.fromHtml(getMessage(), null, getCustomTagHandler()));

        builder.setPositiveButton(getUpdateNowLabel(), listener);
        builder.setNeutralButton(getRemindMeLaterLabel(), listener);
        builder.setNegativeButton(getIgnoreThisVersionLabel(), listener);

        builder.setCancelable(isDialogCancelable());

        AlertDialog dialog = builder.create();
        if (activity != null && !activity.isFinishing()) {
            dialog.show();
        }
    }

    private void setLaunchTimes() {
        int launchTimes = getLaunchTimes();
        launchTimes++;
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(PREF_LAUNCH_TIMES, launchTimes)
                .apply();
    }

    private String getUpdateNowLabel() {
        return updateNowLabel != null ? updateNowLabel : activity.getString(R.string.update);
    }

    public void setUpdateNowLabel(String updateNowLabel) {
        this.updateNowLabel = updateNowLabel;
    }

    private String getRemindMeLaterLabel() {
        return remindMeLaterLabel != null ? remindMeLaterLabel : activity.getString(R.string.later);
    }

    public void setRemindMeLaterLabel(String remindMeLaterLabel) {
        this.remindMeLaterLabel = remindMeLaterLabel;
    }

    private String getIgnoreThisVersionLabel() {
        return ignoreThisVersionLabel != null ? ignoreThisVersionLabel : activity.getString(R.string.ignore);
    }

    public void setIgnoreThisVersionLabel(String ignoreThisVersionLabel) {
        this.ignoreThisVersionLabel = ignoreThisVersionLabel;
    }

    private String getMessage() {
        String defaultMessage = "What's new in this version";
        return message != null ? message : defaultMessage;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private String getTitle() {
        String defaultTitle = "New Update Available";
        return title != null ? title : defaultTitle;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private Drawable getIcon() {
        return icon != null ? icon : getDefaultAppIcon();
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    String getUpdateUrl() {
        return updateUrl != null ? updateUrl : getGooglePlayStoreUrl();
    }

    private void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    private String getVersionContentUrl() {
        return versionContentUrl;
    }

    void setVersionContentUrl(String versionContentUrl) {
        this.versionContentUrl = versionContentUrl;
    }

    int getReminderTimer() {
        return reminderTimer > 0 ? reminderTimer : 1;
    }

    public void setReminderTimer(int minutes) {
        if (minutes > 0) {
            reminderTimer = minutes;
        }
    }

    void updateNow(String url) {
        if (url != null) {
            try {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "is update url correct?" + e);
            }
        }

    }

    void remindMeLater(int reminderTimer) {
        Calendar c = Calendar.getInstance();

        c.add(Calendar.MINUTE, reminderTimer);
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

    private boolean isDialogCancelable() {
        return mDialogCancelable;
    }

    public void setDialogCancelable(boolean dialogCancelable) {
        mDialogCancelable = dialogCancelable;
    }

    interface ActivityListener {
        void isShowUpdateDialog(boolean flag);
    }

    private class AlertDialogButtonListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    if (sCallback != null) {
                        sCallback.onPositive("WVersionManager");
                    }
                    break;
                case AlertDialog.BUTTON_NEUTRAL:
                    if (sCallback != null) {
                        sCallback.onCancelled("WVersionManager");
                    }
                    remindMeLater(getReminderTimer());
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    if (sCallback != null) {
                        sCallback.onNegative("WVersionManager");
                    }
                    break;
            }
        }
    }

    private class VersionContentRequest extends AsyncTask<String, Void, String> {
        Context context;

        VersionContentRequest(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... uri) {
            String path = getVersionContentUrl();
            String result = null;
            try {
                URL u = new URL(path);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                InputStream in = c.getInputStream();
                final ByteArrayOutputStream bo = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                in.read(buffer); // Read from Buffer.
                bo.write(buffer); // Write Into Buffer.
                result = bo.toString();
                bo.close();
            } catch (MalformedURLException e) {
                Log.e("WTF", "Malformed content: " + e.getMessage());
            } catch (ProtocolException e) {
                Log.e("WTF", "Protocol error: " + e.getMessage());
            } catch (IOException e) {
                Log.e("WTF", "IO error: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            mVersionCode = 0;
            String content;
            String packageName;
            if (result != null) {
                try {
                    if (!result.startsWith("{")) { // for response who append with unknown char
                        result = result.substring(1);
                    }
                    String mResult = result;
                    // json format from server:
                    JSONObject json = (JSONObject) new JSONTokener(mResult).nextValue();
                    mVersionCode = json.optInt("version_code");
                    String lang = Locale.getDefault().getLanguage();
                    if (lang.equals("ru")) {
                        content = json.optString("content_ru");
                    } else {
                        content = json.optString("content_en");
                    }
                    packageName = json.optString("package");
                    setUpdateUrl("market://details?id=" + packageName);
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
                        } else {
                            al.isShowUpdateDialog(false);
                        }
                    } else {
                        al.isShowUpdateDialog(false);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "is your server response have valid json format?");
                    al.isShowUpdateDialog(false);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    al.isShowUpdateDialog(false);
                }
            } else {
                al.isShowUpdateDialog(false);
            }
        }
    }

    private class CustomTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output,
                              XMLReader xmlReader) {
            // you may add more tag handler which are not supported by android here
            if ("li".equals(tag)) {
                if (opening) {
                    output.append(" \u2022 ");
                } else {
                    output.append("\n");
                }
            }
        }
    }
}