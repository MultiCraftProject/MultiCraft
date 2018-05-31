package mobi.MultiCraft;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static mobi.MultiCraft.MainActivity.unzipLocation;


public class CopyZipTask extends AsyncTask<String, Void, String> {

    private WeakReference<Context> contextRef;
    private CallBackListener listener;
    private String[] zips;

    CopyZipTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected String doInBackground(String... params) {
        zips = params;
        for (String s : zips) {
            copyAssets(s);
        }
        return "Done";

    }


    @Override
    protected void onPostExecute(String result) {
        listener.updateViews(R.string.loading, View.VISIBLE, View.GONE);
        startUnzipService(zips);

    }

    private void copyAssets(String zipName) {
        String filename = zipName.substring(zipName.lastIndexOf("/") + 1);
        InputStream in;
        OutputStream out;
        try {
            in = contextRef.get().getAssets().open(filename);
            out = new FileOutputStream(zipName);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            Log.e("WTF", "Failed to copy asset file: " + e.getMessage());
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void startUnzipService(String[] file) {
        // Start MyIntentService
        Intent intentMyIntentService = new Intent(contextRef.get(), UnzipService.class);
        intentMyIntentService.putExtra(UnzipService.EXTRA_KEY_IN_FILE, file);
        intentMyIntentService.putExtra(UnzipService.EXTRA_KEY_IN_LOCATION, unzipLocation);
        contextRef.get().startService(intentMyIntentService);

    }

    void setListener(CallBackListener listener) {
        this.listener = listener;
    }
}
