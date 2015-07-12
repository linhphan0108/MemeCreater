package uit.linh.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import uit.linh.ui.R;

/**
 *A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 * Created by linh on 14/06/2015.
 */
public class ImageFetcher extends ImageResizer {
    private static final String TAG = "ImageFetcher";
    private static final String HTTP_CACHE_DIR = "http";
    private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int IO_BUFFER_SIZE = 1024;//1 kB

    private DiskLruCache httpDiskCache;
    private boolean httpDiskCacheStarting = true;
    private final Object httpDiskCacheLock = new Object();
    private static final int DISK_CACHE_INDEX = 0;
    private File httpCacheDir;

    public ImageFetcher(Context context, int width, int height) {
        super(context, width, height);
        init(context);
    }

    public ImageFetcher(Context context, int width) {
        super(context, width);
        init(context);
    }

    private void init(Context context){
        checkConnection(context);
        httpCacheDir = MemoryCache.getDiskCacheDir(context, HTTP_CACHE_DIR);
    }

    @Override
    protected void initDiskCacheInternal() {
        super.initDiskCacheInternal();
        initHttpDiskCache();
    }

    public void initHttpDiskCache() {
        if (!httpCacheDir.exists()){
            if (httpCacheDir.mkdirs());
//                Log.d(TAG, "created http directory disk cache");
        }

        synchronized (httpDiskCacheLock){
            if (MemoryCache.getUsableSpace(httpCacheDir) > HTTP_CACHE_SIZE){
                try {
                    httpDiskCache =  DiskLruCache.open(httpCacheDir, 1, 1, HTTP_CACHE_SIZE);
//                    Log.d(TAG, "HTTP cache initialized");

                } catch (IOException e) {
                    e.printStackTrace();
                    httpDiskCache = null;
                }
            }else{
//                Log.d(TAG, "lack of memory");
            }
            httpDiskCacheStarting = false;
            httpDiskCacheLock.notifyAll();
        }
    }

    @Override
    protected void clearCacheInternal() {
        super.clearCacheInternal();
        synchronized (httpDiskCacheLock){
            if (httpDiskCache != null && !httpDiskCache.isClosed()){
                try {
                    httpDiskCache.delete();
//                    Log.d(TAG, "HTTP cache cleared");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                httpDiskCache = null;
                httpDiskCacheStarting = true;
                initHttpDiskCache();
            }
        }
    }

    @Override
    protected void flushCacheInternal() {
        super.flushCacheInternal();
        synchronized (httpDiskCacheLock){
            if (httpDiskCache != null && !httpDiskCache.isClosed()){
                try {
                    httpDiskCache.flush();
//                    Log.d(TAG, "HTTP cache flushed");

                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.e(TAG, "clearCacheInternal - " + e);
                }
            }
        }
    }

    @Override
    protected void closeCacheInternal() {
        super.closeCacheInternal();
        synchronized (httpDiskCacheLock){
            if (httpDiskCache != null && !httpDiskCache.isClosed()){
                try {
                    httpDiskCache.close();
//                    Log.d(TAG, "HTTP cache closed");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    public Bitmap processBitmap(String url){
        if (url == null || url.equals("")) return null;

        String hashKey = MemoryCache.generateHash(url);
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        DiskLruCache.Snapshot snapshot = null;
        Bitmap bitmap = null;
        synchronized (httpDiskCacheLock){

            while (httpDiskCacheStarting){
                try {
                    httpDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (httpDiskCache != null){
                try {
                    snapshot = httpDiskCache.get(hashKey);
                    if (snapshot == null){
//                        Log.d(TAG, "bitmap is not found in http cache, Downloading...");
                        DiskLruCache.Editor editor = httpDiskCache.edit(hashKey);
                        if (editor != null){
                            if (downloadUrlToStream(url, editor.newOutputStream(DISK_CACHE_INDEX)))
                                editor.commit();
                            else
                                editor.abort();
                        }
                        snapshot = httpDiskCache.get(hashKey);
                    }

                    if(snapshot != null){
                        fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                        fileDescriptor = fileInputStream.getFD();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fileDescriptor == null && fileInputStream != null)
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }

        if (fileDescriptor != null){
            bitmap = ImageResizer.decodeSampleBitmapFromDescriptor(fileDescriptor, imageWidth, imageHeight);
        }
        if (fileInputStream != null){
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private boolean downloadUrlToStream(String stringUrl, OutputStream outputStream) {
        if (checkConnection(mContext))
            setPauseWork(false);

        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            URL url = new URL(stringUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);

            int temp;
            while ((temp = in.read())!= -1){
                out.write(temp);
            }

//            Log.d(TAG, "bitmap is downloaded");
            return true;

        } catch (IOException e) {
//            Log.e(TAG, "Error in downloadBitmap - " + e);
            e.printStackTrace();
        }finally {
            if (urlConnection != null)
                urlConnection.disconnect();
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return false;
    }

    public static Bitmap downloadBitmap(String urlString) throws IOException {
        Bitmap bitmap = null;
        InputStream in;
        URLConnection urlConnection;
        urlConnection = new URL(urlString).openConnection();
        in = urlConnection.getInputStream();
        bitmap = BitmapFactory.decodeStream(in);
        return bitmap;
    }

    protected boolean checkConnection(Context context){
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()){
//            Log.e(TAG, "checkConnection - no connection found");
            new AlertDialog.Builder(context)
                    .setTitle("Warning")
                    .setMessage(context.getResources().getString(R.string.no_network_connection))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).create().show();

            return false;
        }
        return true;
    }
}
