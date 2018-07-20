package mobi.MultiCraft;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class UnzipService extends IntentService {
    public static final String ACTION_UPDATE = "mobi.MultiCraft.UPDATE";
    public static final String EXTRA_KEY_IN_FILE = "file";
    public static final String EXTRA_KEY_IN_LOCATION = "location";
    public static final String ACTION_PROGRESS = "progress";
    private NotificationManager mNotifyManager;
    private int id = 1;
    private int percent = 0;
    private int size;

    public UnzipService() {
        super("mobi.MultiCraft.UnzipService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createNotification();
        unzipLoop(intent);

    }

    private void createNotification() {
        // There are hardcoding only for show it's just strings
        String name = "mobi.MultiCraft";
        String channelId = "MultiCraft channel"; // The user-visible name of the channel.
        String description = "notifications from MultiCraft"; // The user-visible description of the channel.
        Notification.Builder builder;
        if (mNotifyManager == null) {
            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = null;
            if (mNotifyManager != null) {
                mChannel = mNotifyManager.getNotificationChannel(channelId);
            }
            if (mChannel == null) {
                mChannel = new NotificationChannel(channelId, name, importance);
                mChannel.setDescription(description);
                //Configure the notification channel, NO SOUND
                mChannel.setSound(null, null);
                mChannel.enableLights(false);
                mChannel.enableVibration(false);
                mNotifyManager.createNotificationChannel(mChannel);
            }
            builder = new Notification.Builder(this, channelId);
            builder.setContentTitle(getString(R.string.notification_title))  // required
                    .setSmallIcon(R.drawable.update) // required
                    .setContentText(getString(R.string.notification_description)); // required
        } else {
            builder = new Notification.Builder(this);
            builder.setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_description))
                    .setSmallIcon(R.drawable.update);
        }
        mNotifyManager.notify(id, builder.build());
    }

    private void unzipLoop(Intent intent) {
        String[] zips = intent.getStringArrayExtra(EXTRA_KEY_IN_FILE);
        String location = intent.getStringExtra(EXTRA_KEY_IN_LOCATION);
        size = getSummarySize(zips);
        try {
            for (String z : zips) {
                unzipFile(z, location);
            }
        } catch (IOException e) {
            Log.e("WTF", e.getMessage() == null ? "Unzip failed" : e.getMessage());
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void unzipFile(String zipFile, String targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory()) {
                    percent++;
                    continue;
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, count);
                        percent++;
                        int progress = 100 * percent / size;
                        publishProgress(progress);
                    }
                } finally {
                    fileOutputStream.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    private void publishProgress(int progress) {
        Intent intentUpdate = new Intent(ACTION_UPDATE);
        intentUpdate.putExtra(ACTION_PROGRESS, progress);
        sendBroadcast(intentUpdate);
    }

    private int getSummarySize(String[] zips) {
        int size = 0;
        for (String z : zips) {
            try {
                ZipFile zipSize = new ZipFile(z);
                size += zipSize.size();
            } catch (IOException e) {
                Log.e("WTF", e.getMessage() == null ? "Unzip failed" : e.getMessage());
            }
        }
        return size;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotifyManager.cancel(id);
        publishProgress(-1);
    }
}