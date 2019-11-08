package com.multicraft.game;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.bugsnag.android.Bugsnag;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class CopyZipTask extends AsyncTask<String, Void, String[]> implements DialogsCallback {

    private final WeakReference<Context> contextRef;
    private CallBackListener listener;
    private boolean isCancel = false;

    CopyZipTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    protected String[] doInBackground(String... params) {
        while (!isCancel)
            copyAssets(params);
        return params;
    }

    @Override
    protected void onPostExecute(String[] result) {
        listener.updateViews(R.string.loading, View.VISIBLE, View.GONE);
        startUnzipService(result);

    }

    private void copyAsset(String zipName) throws IOException {
        String filename = zipName.substring(zipName.lastIndexOf("/") + 1);
        InputStream in = contextRef.get().getAssets().open(filename);
        OutputStream out = new FileOutputStream(zipName);
        copyFile(in, out);
        in.close();
        out.flush();
        out.close();
    }

    private void copyAssets(String[] zips) {
        try {
            for (String zipName : zips)
                copyAsset(zipName);
            isCancel = true;
        } catch (IOException e) {
            Bugsnag.notify(e);
            isCancel = true;
            cancel(true);
            if (Objects.requireNonNull(e.getMessage()).contains("ENOSPC"))
                showRestartDialog("ENOSPC");
            else
                showRestartDialog("UKNWN");
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
            out.write(buffer, 0, read);
    }

    private void showRestartDialog(final String source) {
        String message;
        if ("ENOSPC".equals(source))
            message = contextRef.get().getString(R.string.no_space);
        else
            message = contextRef.get().getString(R.string.restart);
        final AlertDialogHelper dialogHelper = new AlertDialogHelper((AppCompatActivity) contextRef.get());
        dialogHelper.setListener(this);
        dialogHelper.setMessage(message);
        dialogHelper.setButtonPositive(contextRef.get().getString(android.R.string.ok));
        ((AppCompatActivity) contextRef.get()).runOnUiThread(() -> dialogHelper.showAlert(source));
    }

    private void startUnzipService(String[] file) {
        // Start MyIntentService
        Intent intentMyIntentService = new Intent(contextRef.get(), UnzipService.class);
        intentMyIntentService.putExtra(UnzipService.EXTRA_KEY_IN_FILE, file);
        contextRef.get().startService(intentMyIntentService);
    }

    private void restartApp() {
        Context context = contextRef.get().getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        int mPendingIntentId = 1337;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null)
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
        System.exit(0);
    }

    void setListener(CallBackListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPositive(String source) {
        restartApp();
    }

    @Override
    public void onNegative(String source) {
    }

    @Override
    public void onNeutral(String source) {
    }
}