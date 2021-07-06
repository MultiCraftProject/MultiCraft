package com.multicraft.game;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.multicraft.game.helpers.ApiLevelHelper.isOreo;
import static com.multicraft.game.helpers.Constants.FILES;
import static com.multicraft.game.helpers.Utilities.getLocationByZip;

public class UnzipService extends JobIntentService {
	public static final String ACTION_UPDATE = "com.multicraft.game.UPDATE";
	public static final String EXTRA_KEY_IN_FILE = "com.multicraft.game.file";
	public static final String ACTION_PROGRESS = "com.multicraft.game.progress";
	public static final String ACTION_FAILURE = "com.multicraft.game.failure";
	public static final int UNZIP_SUCCESS = -1;
	public static final int UNZIP_FAILURE = -2;
	private static final int JOB_ID = 1;
	private final int id = 1;
	private NotificationManager mNotifyManager;
	private String failureMessage;
	private boolean isSuccess = true;

	public static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, UnzipService.class, JOB_ID, work);
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		createNotification();
		unzip(intent);
	}

	private void createNotification() {
		String name = "com.multicraft.game";
		String channelId = "MultiCraft channel";
		String description = "notifications from MultiCraft";
		Notification.Builder builder;
		if (mNotifyManager == null)
			mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (isOreo()) {
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel mChannel = null;
			if (mNotifyManager != null)
				mChannel = mNotifyManager.getNotificationChannel(channelId);
			if (mChannel == null) {
				mChannel = new NotificationChannel(channelId, name, importance);
				mChannel.setDescription(description);
				// Configure the notification channel, NO SOUND
				mChannel.setSound(null, null);
				mChannel.enableLights(false);
				mChannel.enableVibration(false);
				mNotifyManager.createNotificationChannel(mChannel);
			}
			builder = new Notification.Builder(this, channelId);
		} else
			builder = new Notification.Builder(this);
		builder.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_description))
				.setSmallIcon(R.drawable.update);
		mNotifyManager.notify(id, builder.build());
	}

	private void unzip(Intent intent) {
		List<String> zips;
		if (intent != null)
			zips = intent.getStringArrayListExtra(EXTRA_KEY_IN_FILE);
		else
			zips = new ArrayList<>(Collections.singletonList(FILES));
		String cacheDir = getCacheDir().toString();
		int per = 0;
		int size = getSummarySize(zips, cacheDir);
		byte[] readBuffer = new byte[8192];
		for (String zip : zips) {
			File zipFile = new File(cacheDir, zip);
			LocalFileHeader localFileHeader;
			int readLen;
			try (FileInputStream fileInputStream = new FileInputStream(zipFile);
			     ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
				String location = getLocationByZip(this, zip);
				while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
					String fileName = localFileHeader.getFileName();
					if (localFileHeader.isDirectory()) {
						++per;
						new File(location, fileName).mkdirs();
					} else {
						File extractedFile = new File(location, fileName);
						publishProgress(100 * ++per / size);
						try (OutputStream outputStream = new FileOutputStream(extractedFile)) {
							while ((readLen = zipInputStream.read(readBuffer)) != -1) {
								outputStream.write(readBuffer, 0, readLen);
							}
						}
					}
				}
			} catch (IOException | NullPointerException e) {
				failureMessage = e.getLocalizedMessage();
				isSuccess = false;
			}
		}
	}

	private void publishProgress(int progress) {
		Intent intentUpdate = new Intent(ACTION_UPDATE);
		intentUpdate.putExtra(ACTION_PROGRESS, progress);
		if (!isSuccess) intentUpdate.putExtra(ACTION_FAILURE, failureMessage);
		sendBroadcast(intentUpdate);
	}

	private int getSummarySize(List<String> zips, String path) {
		int size = 1;
		for (String z : zips) {
			try {
				ZipFile zipFile = new ZipFile(new File(path, z));
				size += zipFile.getFileHeaders().size();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return size;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mNotifyManager != null)
			mNotifyManager.cancel(id);
		publishProgress(isSuccess ? UNZIP_SUCCESS : UNZIP_FAILURE);
	}
}
