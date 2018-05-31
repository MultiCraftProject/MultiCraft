package mobi.MultiCraft;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

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
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                Log.e("WTF", "delete files failed: " + e.getLocalizedMessage());
            }
        }
    }

    void setListener(CallBackListener listener) {
        this.listener = listener;
    }
}
