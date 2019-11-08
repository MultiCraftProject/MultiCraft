package com.multicraft.game;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bugsnag.android.Bugsnag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.multicraft.game.MainActivity.zipLocations;

public class UnzipService extends IntentService {
    public static final String ACTION_UPDATE = "com.multicraft.game.UPDATE";
    public static final String EXTRA_KEY_IN_FILE = "file";
    public static final String ACTION_PROGRESS = "progress";
    private final int id = 1;
    private NotificationManager mNotifyManager;

    public UnzipService() {
        super("com.multicraft.game.UnzipService");
    }

    private void isDir(String dir, String unzipLocation) {
        File f = new File(unzipLocation + dir);

        if (!f.isDirectory())
            f.mkdirs();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createNotification();
        unzip(intent);
    }

    private String getSettings() {
        return getString(R.string.gdpr_main_text);
    }

    private void createNotification() {
        // There are hardcoding only for show it's just strings
        String name = "com.multicraft.game";
        String channelId = "MultiCraft channel"; // The user-visible name of the channel.
        String description = "notifications from MultiCraft"; // The user-visible description of the channel.
        Notification.Builder builder;
        if (mNotifyManager == null)
            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = null;
            if (mNotifyManager != null)
                mChannel = mNotifyManager.getNotificationChannel(channelId);
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

    private void unzip(Intent intent) {
        String[] zips = intent.getStringArrayExtra(EXTRA_KEY_IN_FILE);
        int per = 0;
        int size = getSummarySize(Objects.requireNonNull(zips));
        for (String zip : zips) {
            File zipFile = new File(zip);
            int readLen;
            byte[] readBuffer = new byte[8192];
            try (FileInputStream fileInputStream = new FileInputStream(zipFile);
                 ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
                ZipEntry ze;
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        ++per;
                        isDir(ze.getName(), zipLocations.get(zip));
                    } else {
                        publishProgress(100 * ++per / size);
                        try (OutputStream outputStream = new FileOutputStream(zipLocations.get(zip) + ze.getName())) {
                            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                                outputStream.write(readBuffer, 0, readLen);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Bugsnag.notify(e);
            } catch (IOException e) {
                Bugsnag.notify(e);
            }
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
                Bugsnag.notify(e);
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
