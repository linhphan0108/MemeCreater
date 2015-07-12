package uit.linh.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;

/**
 *
 * Created by linh on 11/06/2015.
 */
public class JsonDownloadTask extends AsyncTask<String, Integer, JSONArray> {

    private static final String TAG = "JsonDownloadTask";
    AsyncTaskInteractionListener asyncTaskInteractionListener;
    final ProgressDialog dialog;
    protected IOException ex;

    public JsonDownloadTask(Context context) {
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "downloading");

        dialog.setCancelable(false);
        dialog.setMessage("Connecting to server...");
        dialog.show();
    }

    @Override
    protected JSONArray doInBackground(String... strings){
        String url = strings[0];
        JSONArray jsonArray = null;
//        try {
//            try {
//                jsonArray = JsonReader.downloadJson(url);
//            } catch (JSONException e) {
//                dialog.cancel();
////                Log.e("err", "JSONException");
//                e.printStackTrace();
//            }
//        }catch (ConnectException e){
//            dialog.cancel();
////            Log.e("err", "ConnectException");
//
//        }catch (final IOException e) {
////            Log.e("err", "IOException");
//            e.printStackTrace();
//
//            dialog.cancel();
//            this.ex = e;
//            this.cancel(false);
//            return null;
//        }
        return jsonArray;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        super.onPostExecute(jsonArray);
        dialog.cancel();
        if (jsonArray != null){
//            Log.d(TAG, "json has downloaded successfully");
        }else{
//            Log.d(TAG, "failed to load json");
            asyncTaskInteractionListener.onErrorConnection(this.ex);
        }
        asyncTaskInteractionListener.onDownloadComplete(jsonArray);
    }

    @Override
    protected void onCancelled(JSONArray jsonArray) {
        super.onCancelled(jsonArray);
        if (this.ex != null)
            asyncTaskInteractionListener.onErrorConnection(this.ex);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (this.ex != null)
            asyncTaskInteractionListener.onErrorConnection(this.ex);

    }

    public void setONAsyncTaskInteractionListener(AsyncTaskInteractionListener asyncTaskInteractionListener){
        this.asyncTaskInteractionListener = asyncTaskInteractionListener;
    }

    public interface AsyncTaskInteractionListener{
        public void onDownloadComplete(JSONArray jsonObject);
        public void onErrorConnection(IOException e);
    }
}