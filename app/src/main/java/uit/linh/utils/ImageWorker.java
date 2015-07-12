package uit.linh.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

/**
 *
 * Created by linh on 01/06/2015.
 */
public abstract class ImageWorker {

    private static final String TAG = "ImageWorker";

    protected Context mContext;
    private Resources mresources;
    private  MemoryCache imageCache;
    private MemoryCache.ImageCacheParams imageCacheParams;
    private Bitmap bitmapLoading;

    private static final Object pauseWorkLock = new Object();
    protected boolean pauseWork = false;
    private boolean exitTasksEarly = false;

    private static int imageHeight = 150; //


    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;


    public ImageWorker(Context context) {
        this.mContext = context;
        this.mresources = context.getResources();
    }

    public void addImageCache(Context context, MemoryCache.ImageCacheParams cacheParams){
        imageCache = new MemoryCache(cacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    public void addImageCache(MemoryCache.ImageCacheParams cacheParams){
        imageCache = new MemoryCache(cacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * Load an image specified by the data into an ImageView
     * If image is cached in memory, it will set immediately. otherwise an AsyncTask will be created
     * to asynchronously load the bitmap.
     * @param data The URL of the image to download.
     * @param imageView he ImageView to bind the downloaded image to.
     */
    public void loadImage(String data, ImageView imageView){
        if(data == null || imageView == null){
            return;
        }
        String url = String.valueOf(data);
        BitmapDrawable drawableCache = null;
        if (imageCache != null) {
            //attempts to get bitmap drawable from memory cache.
            drawableCache = imageCache.getBitmapFromMemoryCache(url);
        }
        if (drawableCache !=null){
            loadImageToView(drawableCache, imageView);

        }else if (cancelPotentialWork(data, imageView)) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask( data, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mresources, bitmapLoading, bitmapWorkerTask);
            imageView.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute();
        }

//        Bitmap bitmap = imageCache.getBitmapFromDiskCache(url);
//        if (bitmap != null) {
//            drawableCache = new BitmapDrawable(mresources, bitmap);
//            loadImageToView(drawableCache, imageView);
//            return;
//        }


    }

    public void setLoadingBitmap(int resourceId){
        bitmapLoading = BitmapFactory.decodeResource(mresources, resourceId);
    }

    public void setExitTasksEarly(boolean tasksEarly){
        exitTasksEarly = tasksEarly;
        setPauseWork(false);
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null){
            bitmapWorkerTask.cancel(true);
//            Log.d(TAG, "cancelWork - cancelled work for ");
        }
    }

    public static boolean cancelPotentialWork(Object data, ImageView imageView){
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null){
            final String bitmapData = (String) bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(data)){
                // Cancel previous task
                //the cancel method of the AsyncTask class to stop the download in progress.
                // It returns true most of the time, so that the download can be started in download.
                bitmapWorkerTask.cancel(true);
            }else{
                // The same work is already in progress
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    protected abstract Bitmap processBitmap(Object data);

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView){
        if (imageView != null){
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable){
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static void loadImageToView(BitmapDrawable drawable, ImageView imageView){
        if (imageView != null && drawable != null){
            imageView.setImageDrawable(drawable);
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    //===
    private class BitmapWorkerTask extends AsyncTask<Void, Void, BitmapDrawable> {
        private final WeakReference<ImageView> imageViewReference;
        private Object data;


        public BitmapWorkerTask(Object data, ImageView imageView) {
            this.data = data;
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected BitmapDrawable doInBackground(Void... params) {
//            Log.d(TAG, "doInBackground - starting work");
            String url = String.valueOf(data);
//            Log.e(TAG, url);
            BitmapDrawable drawable = null;
            Bitmap bitmap = null;

            //wait here if work is paused and the task is not cancelled.
            synchronized (pauseWorkLock) {
                while (pauseWork && !isCancelled()) {
                    try {
//                        Log.e(TAG, "do in background, waiting");
                        pauseWorkLock.wait();
//                        Log.e(TAG, "do in background after waiting");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (imageCache != null && !isCancelled() && getAttachedImageView() != null && !exitTasksEarly) {
                bitmap = imageCache.getBitmapFromDiskCache(url);
                if (bitmap != null){
//                    Log.d(TAG, "getBitmapFromDiskCache()");
                }
            }

            //if the image cache is not found and this task has not been cancelled by another thread ad the ImageView
            //that was originally bound to this task is still bound back to this task and our exit early flag is not set
            //then download the image from the internet.
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null && !exitTasksEarly) {
                bitmap = processBitmap(data);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                drawable = new BitmapDrawable(mresources, bitmap);
                if (imageCache != null)
                    imageCache.addBitmap2Cache(url, drawable);
            }

//            Log.d(TAG, "doInBackground - finished work");
            return drawable;
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            super.onPostExecute(drawable);

            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || exitTasksEarly){
                drawable = null;
            }
            if (drawable !=null) {
                ImageView imageView = getAttachedImageView();
                loadImageToView(drawable, imageView);
//                Log.d(TAG, "image loaded successfully: "+ MemoryCache.generateHash(String.valueOf(data)));
            }else{
//                Log.d(TAG, "empty");
            }
        }

        @Override
        protected void onCancelled(BitmapDrawable drawable) {
            super.onCancelled(drawable);
            synchronized (pauseWorkLock) {
//                Log.e(TAG, "on cancelled");
                pauseWorkLock.notifyAll();
            }
        }

        private ImageView getAttachedImageView(){
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask){
                return imageView;
            }
            return null;
        }
    }

    public static class AsyncDrawable extends BitmapDrawable {
        final private WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;


        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            this.bitmapWorkerTaskWeakReference = new WeakReference<>(bitmapWorkerTask);

        }

        public BitmapWorkerTask getBitmapWorkerTask(){
            return bitmapWorkerTaskWeakReference.get();
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pause){
        synchronized (pauseWorkLock){
            pauseWork = pause;
            if (!pauseWork){
                pauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (imageCache != null) {
            imageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (imageCache != null) {
            imageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (imageCache != null) {
            imageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (imageCache != null) {
            imageCache.close();
            imageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }

}
