package mobi.MultiCraft;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckConnectionTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Context> contextRef;
    private CallBackListener listener;

    CheckConnectionTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    void setListener(CallBackListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Context context = contextRef.get();
        return context != null && isReachable();
    }

    @Override
    protected void onPostExecute(Boolean isStart) {
        listener.onEvent("CheckConnectionTask", isStart.toString());
    }

    private boolean isGoogleAvailable(String url, int timeout) {
        try {
            HttpURLConnection urlc = (HttpURLConnection)
                    (new URL(url)
                            .openConnection());
            urlc.setRequestProperty("User-Agent", "Android");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(timeout);
            urlc.connect();
            return urlc.getResponseCode() == 204 && urlc.getContentLength() == 0;
        } catch (IOException e) {
            Log.e("WTF", "IOException " + e.getMessage());
        }
        return false;
    }

    private boolean isReachable() {
        return isGoogleAvailable("http://clients3.google.com/generate_204", 1500) ||
                isGoogleAvailable("http://g.cn/generate_204", 1000);
    }
}

