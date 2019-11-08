package com.multicraft.game;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

class CheckConnectionTask extends AsyncTask<Void, Void, Boolean> {
    private final WeakReference<Context> contextRef;
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

    private boolean isInternetAvailable(String url) {
        try {
            HttpURLConnection urlc = (HttpURLConnection)
                    (new URL(url)
                            .openConnection());
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500);
            urlc.connect();
            return urlc.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT || urlc.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            // nothing
        }
        return false;
    }

    private boolean isReachable() {
        return isInternetAvailable("http://clients3.google.com/generate_204") ||
                isInternetAvailable("http://servers.multicraft.world");
    }
}
