package mobi.MultiCraft;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
    public final String TAG = UnzipService.class.getSimpleName();
    private NotificationManager mNotifyManager;
    private int id = 1;

    public UnzipService() {
        super("mobi.MultiCraft.UnzipService");
    }

    private void isDir(String dir, String unzipLocation) {
        File f = new File(unzipLocation + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createNotification();
        unzip(intent);

    }

    private void createNotification() {
        // There are hardcoding only for show it's just strings
        String name = "mobi.MultiCraft";
        String channelId = "MultiCraft channel"; // The user-visible name of the channel.
        String description = "notifications from MultiCraft"; // The user-visible description of the channel.
        NotificationCompat.Builder builder;
        if (mNotifyManager == null) {
            mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = mNotifyManager.getNotificationChannel(channelId);
            if (mChannel == null) {
                mChannel = new NotificationChannel(channelId, name, importance);
                mChannel.setDescription(description);
                //Configure the notification channel, NO SOUND
                mChannel.setSound(null, null);
                mChannel.enableLights(false);
                mChannel.enableVibration(false);
                mNotifyManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, channelId);
            builder.setContentTitle(getString(R.string.notification_title))  // required
                    .setSmallIcon(R.drawable.update) // required
                    .setContentText(getString(R.string.notification_description)); // required
        } else {
            builder = new NotificationCompat.Builder(this);
            builder.setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_description))
                    .setSmallIcon(R.drawable.update);
        }
        mNotifyManager.notify(id, builder.build());
    }

    private void unzip(Intent intent) {
        String[] file = intent.getStringArrayExtra(EXTRA_KEY_IN_FILE);
        String location = intent.getStringExtra(EXTRA_KEY_IN_LOCATION);
        int per = 0;
        int size = getSummarySize(file);
        for (String f : file) {
            try {
                try {
                    FileInputStream fin = new FileInputStream(f);
                    ZipInputStream zin = new ZipInputStream(fin);
                    ZipEntry ze;
                    while ((ze = zin.getNextEntry()) != null) {
                        if (ze.isDirectory()) {
                            per++;
                            isDir(ze.getName(), location);
                        } else {
                            per++;
                            int progress = 100 * per / size;
                            // send update
                            publishProgress(progress);
                            FileOutputStream f_out = new FileOutputStream(location + ze.getName());
                            byte[] buffer = new byte[8192];
                            int len;
                            while ((len = zin.read(buffer)) != -1) {
                                f_out.write(buffer, 0, len);
                            }
                            f_out.close();
                            zin.closeEntry();
                            f_out.close();
                        }
                    }
                    zin.close();
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
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
                Log.e(TAG, e.getLocalizedMessage());
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