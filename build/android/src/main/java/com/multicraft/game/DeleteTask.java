package com.multicraft.game;

import android.os.AsyncTask;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;


public class DeleteTask extends AsyncTask<String, Void, Void> {

    private CallBackListener listener;
    private String location;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.updateViews(R.string.rm_old, View.VISIBLE, View.VISIBLE);
    }

    @Override
    protected Void doInBackground(String... params) {
        location = params[0];
        for (String p : params) {
            deleteFiles(p);
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        listener.onEvent("DeleteTask", location);
    }

    private void deleteFiles(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
    }

    void setListener(CallBackListener listener) {
        this.listener = listener;
    }
}
