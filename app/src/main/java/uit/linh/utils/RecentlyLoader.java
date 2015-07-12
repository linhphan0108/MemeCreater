package uit.linh.utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * Created by linh on 13/06/2015.
 */
public class RecentlyLoader{

    public static ArrayList<File> getFiles(File dir){
        if (!checkSdcard())
            return null;

        ArrayList<File> result = new ArrayList<>();

        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    result.add(files[i]);
                }
            }
        }
        return result;
    }



    //checking sdcard is available or not
    private static boolean checkSdcard() {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
                || !Environment.isExternalStorageRemovable();
    }
}
