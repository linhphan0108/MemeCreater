package uit.linh.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * Created by linh on 02/06/2015.
 */
public class MemoryCache {
    private  static final String TAG = "ImageCache";
    private static final int MEMORY_CACHE_AVAILABLE= (int) Runtime.getRuntime().maxMemory(); //Get max available VM memory.
    private static final int DEFAULT_MEMORY_CACHE_SIZE = MEMORY_CACHE_AVAILABLE/8;// Use 1/8th of the available memory for memory cache.
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

    // Compression settings when writing images to disk cache
    private static final Bitmap.CompressFormat DEFAULT_COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final int DISK_CACHE_INDEX = 0;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    private LruCache<String, BitmapDrawable> memoryCache;
    private DiskLruCache diskCache;
    private ImageCacheParams imageCacheParams;
    private  final Object diskCacheLock = new Object();
    private static boolean diskCacheStarting = true;

    public static Resources resources;



    public MemoryCache(ImageCacheParams cacheParams) {
        init(cacheParams);
    }

    private void init(ImageCacheParams cacheParams) {
        this.imageCacheParams = cacheParams;

        if (cacheParams.memoryCacheEnabled){
//            Log.d(TAG, "Memory cache created (size = " + cacheParams.memCacheSize + ")");
            memoryCache = new LruCache<String, BitmapDrawable>(cacheParams.diskCacheSize){
                /**
                 * Notify the removed entry that is no longer being cached
                 */
                @Override
                protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
                    super.entryRemoved(evicted, key, oldValue, newValue);
//                    Log.d(TAG, "remove entry: "+ key);
                }

                /**
                 * measure item size in kilobytes
                 */
                @Override
                protected int sizeOf(String key, BitmapDrawable value) {
                    final int bitmapSize = value.getBitmap().getByteCount();
                    return bitmapSize == 0 ? 1 : bitmapSize;
                }
            };
        }

        if (cacheParams.diskCacheEnabled)
            initDiskCache();
    }


    /**
     *initializes the disk cache. notes that this includes disk access so this should not be executed on
     * the main/ui thread. By default the MememoryCache doesn't initialize the disk cache when it is created.
     * instead you you should call initDiskCache() to initialize it on the background thread.
     */
    public void initDiskCache(){
        //setup Disk Cache
        synchronized (diskCacheLock){
            if (diskCache == null || diskCache.isClosed()){
                File diskCacheDir = imageCacheParams.diskCacheDir;
//                File diskCacheDir = getDiskCacheDir(context, "http");
                if (imageCacheParams.diskCacheEnabled && diskCacheDir != null){
                    if (!diskCacheDir.exists()){
                        if (diskCacheDir.mkdirs());
//                            Log.e(TAG, "create folder cache: " + diskCacheDir.getAbsolutePath());
                    }
                    if (getUsableSpace(diskCacheDir) > imageCacheParams.diskCacheSize) {
                        try {
                            diskCache = DiskLruCache.open(diskCacheDir, 1, 1, imageCacheParams.diskCacheSize);
//                            Log.d(TAG, "Disk cache initialized");
                        } catch (IOException e) {
                            e.printStackTrace();
                            imageCacheParams.diskCacheDir = null;
//                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    }else{
//                        Log.e(TAG, "lack of disk memory cache");
                    }
                }
            }
            diskCacheStarting = false;
            diskCacheLock.notifyAll();
//            Log.d(TAG, "disk cache lock is notified");
        }
    }


    /**
     * adds bitmap to both memory and disk cache
//     * @param key unique identifier for the bitmap to store
//     * @param drawable the bitmap drawable to store
     */
    public void addBitmap2Cache(String key, BitmapDrawable drawable){
        if (key == null || drawable == null)
            return;

        // Add to memory cache as before
        if (memoryCache != null){
            final String hashKey = generateHash(key);
            if (getBitmapFromMemoryCache(hashKey) == null){
                memoryCache.put(hashKey, drawable);
//                Log.d(TAG, "bitmap has stored in memory");
            }else{
//                Log.d(TAG, "the bitmap cache has existed");
            }
        }

        // Also add to disk cache
        synchronized (diskCacheLock){
            if (diskCache != null){
                final String hashKey = generateHash(key);
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = diskCache.get(hashKey);
                    if (snapshot == null){
                        final DiskLruCache.Editor editor = diskCache.edit(hashKey);
                        if (editor != null){
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            drawable.getBitmap().compress(imageCacheParams.compressFormat, imageCacheParams.compressQuality, out);
                            editor.commit();
                            out.close();
                        }
                    }else{
                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    public void addBitmap2Cache(String data, BitmapDrawable value) {
//        //BEGIN_INCLUDE(add_bitmap_to_cache)
//        if (data == null || value == null) {
//            return;
//        }
//        final String key = generateHash(data);
//
//        // Add to memory cache
//        if (memoryCache != null) {
//            memoryCache.put(key, value);
//        }
//
//        synchronized (diskCacheLock) {
//            // Add to disk cache
//            if (diskCache != null) {
//
//                OutputStream out = null;
//                try {
//                    DiskLruCache.Snapshot snapshot = diskCache.get(key);
//                    if (snapshot == null) {
//                        final DiskLruCache.Editor editor = diskCache.edit(key);
//                        if (editor != null) {
//                            out = editor.newOutputStream(DISK_CACHE_INDEX);
//                            value.getBitmap().compress(
//                                    imageCacheParams.compressFormat, imageCacheParams.compressQuality, out);
//                            editor.commit();
//                            out.close();
//                        }
//                    } else {
//                        snapshot.getInputStream(DISK_CACHE_INDEX).close();
//                    }
//                } catch (final IOException e) {
//                    Log.e(TAG, "addBitmapToCache - " + e);
//                } catch (Exception e) {
//                    Log.e(TAG, "addBitmapToCache - " + e);
//                } finally {
//                    try {
//                        if (out != null) {
//                            out.close();
//                        }
//                    } catch (IOException e) {}
//                }
//            }
//        }
//        //END_INCLUDE(add_bitmap_to_cache)
//    }

    /**
     * get drawable from memory cache
     * @param key the unique identifier for which item to get
     * @return the bitmap drawable if found in cache, otherwise return null
     */
    public BitmapDrawable getBitmapFromMemoryCache(String key){
        if (key == null) return null;

        String hashKey = generateHash(key);
        BitmapDrawable drawable = null;
        if (memoryCache != null) {
             drawable = memoryCache.get(hashKey);
            if (drawable != null);
//                Log.d(TAG, "Memory cache hit");
        }
        return drawable;
    }

    /**
     * get bitmap bitmap from disk cache
//     * @param key the unique identifier for which item to get
     * @return the bitmap drawable if found in disk cache, otherwise return null.
     */
    public Bitmap getBitmapFromDiskCache(String key){
        if(key == null) return null;

        String hashKey = generateHash(key);
        Bitmap bitmap = null;

        synchronized (diskCacheLock){
            // Wait while disk cache is started from background thread
            while (diskCacheStarting){
                try {
                    diskCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (diskCache != null){
                InputStream in = null;
                try {
                    DiskLruCache.Snapshot snapshot = diskCache.get(hashKey);//47ccbea24c7490c918144477ebada3a9
                    if (snapshot != null){
//                        Log.d(TAG, "Disk cache hit");
                        in = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (in != null){
                            FileDescriptor fileDescriptor = ((FileInputStream) in).getFD();
                            bitmap = ImageResizer.decodeSampleBitmapFromDescriptor(fileDescriptor, 1000, 1000);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
            return bitmap;
        }
    }


//    public Bitmap getBitmapFromDiskCache(String data) {
//        //BEGIN_INCLUDE(get_bitmap_from_disk_cache)
//        final String key = generateHash(data);
//        Bitmap bitmap = null;
//
//        synchronized (diskCacheLock) {
//            while (diskCacheStarting) {
//                try {
//                    diskCacheLock.wait();
//                } catch (InterruptedException e) {}
//            }
//            if (diskCache != null) {
//                InputStream inputStream = null;
//                try {
//                    final DiskLruCache.Snapshot snapshot = diskCache.get(key);
//                    if (snapshot != null) {
////                        if (BuildConfig.DEBUG) {
//                            Log.d(TAG, "Disk cache hit");
////                        }
//                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
//                        if (inputStream != null) {
//                            FileDescriptor fd = ((FileInputStream) inputStream).getFD();
//
//                            // Decode bitmap, but we don't want to sample so give
//                            // MAX_VALUE as the target dimensions
//                            bitmap = ImageResizer.decodeSampleBitmapFromDescriptor(
//                                    fd, 1000, 1000);
//                        }
//                    }
//                } catch (final IOException e) {
//                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
//                } finally {
//                    try {
//                        if (inputStream != null) {
//                            inputStream.close();
//                        }
//                    } catch (IOException e) {}
//                }
//            }
//            return bitmap;
//        }
//        //END_INCLUDE(get_bitmap_from_disk_cache)
//    }


    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache(){
        if (memoryCache != null){
            memoryCache.evictAll();
//            Log.d(TAG, "Memory cache cleared");
        }

        synchronized (diskCacheLock){
            diskCacheStarting = true;
            if(diskCache != null && !diskCache.isClosed()) {
                try {
                    diskCache.delete();
//                    Log.d(TAG, "Disk cache cleared");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                diskCache = null;
                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush(){
        synchronized (diskCacheLock){
            if(diskCache != null){
                try {
                    diskCache.flush();
//                    Log.d(TAG, "Disk cache flushed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * closes the disk cache associated with this MemoryCache object. Note that this include disk access
     * so this should not be executed on the main/ui thread
     */
    public void close(){
        synchronized (diskCacheLock){
            if (diskCache != null && !diskCache.isClosed()){
                try {
                    diskCache.close();
                    diskCache = null;
//                    Log.d(TAG, "Disk cache closed");
                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName){
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                !isExternalStorageRemoveable() ? getExternalCacheDir(context).getPath() :  context.getCacheDir().getPath();
//        Log.e(TAG, "cache path - " + cachePath);
        return new File(cachePath + File.separator + uniqueName);
    }


    public static boolean isExternalStorageRemoveable(){
        return Environment.isExternalStorageRemovable();
    }

    public static File getExternalCacheDir(Context context){
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }
        //before Froyo we need to construct the external cache directory for ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static long getUsableSpace(File path){
        if (Utils.hasGingerbread()){
            return path.getUsableSpace();
        }
        final StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSize() * statFs.getAvailableBlocks();
    }

    /**
     * change a string url to a hash string url.
     * @param key
     * @return
     */
    public static String generateHash(String key){
        if (key == null) return null;

        String hash = null;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key.getBytes());
            hash = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private static String bytesToHexString(byte[] bytes){
        // http://stackoverflow.com/questions/332079
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<bytes.length; i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1){
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * a holder class that contains cache parameters
     */
    public static class ImageCacheParams{
        public int memCacheSize = DEFAULT_MEMORY_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;

        public File diskCacheDir;
        public Bitmap.CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = MemoryCache.DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;


        public void setMemCacheSizePercent(float percent) {
            if (percent < 0.1f || percent > 0.8f){
                throw new IllegalArgumentException("percent must be between 0.1 and 0.8");
            }
            this.memCacheSize = Math.round(Runtime.getRuntime().maxMemory() * percent);
        }

        /**
         * Create a set of image cache parameters that can be provided to
         * {@link MemoryCache#(android.support.v4.app.FragmentManager, ImageCacheParams)} or
         * {@link ImageWorker#(android.support.v4.app.FragmentManager, ImageCacheParams)}.
         * @param context A context to use.
         * @param diskCacheDirectoryName A unique subdirectory name that will be appended to the
         *                               application cache directory. Usually "cache" or "images"
         *                               is sufficient.
         */
        public ImageCacheParams(Context context, String diskCacheDirectoryName) {
            diskCacheDir = getDiskCacheDir(context, diskCacheDirectoryName);
        }
    }
}
