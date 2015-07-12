package uit.linh.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import uit.linh.debugs.Logger;

/**
 *
 * Created by linh on 12/06/2015.
 */
public class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    public static boolean checkConnection(Context context){
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()){
            if (Logger.DEBUG_MODE)
                Log.e(TAG, "checkConnection - no connection found");
            return false;
        }
        return true;
    }
}
