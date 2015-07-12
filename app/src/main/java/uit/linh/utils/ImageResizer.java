package uit.linh.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * the subclass of @link ImageWorker that resize images from resource given a target with and height.
 * Created by linh on 03/06/2015.
 */
public class ImageResizer extends ImageWorker{
    final private static String TAG = "ImageResizer";
    protected int imageWidth;
    protected int imageHeight; //


    public ImageResizer(Context context, int width, int height) {
        super(context);
        this.imageWidth = width;
        this.imageHeight = height;
    }

    /**
     * initialize providing a single target image size used for both width and height.
     */
    public ImageResizer(Context context, int imageSize) {
        super(context);
        this.imageWidth = imageSize;
        this.imageHeight = imageSize;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return null;
    }

    private void setImageSize(int size){
        setImageSize(size, size);
    }

    private void setImageSize(int width, int height){
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public static int calculateInSampleSize(BitmapFactory.Options bmOptions, int reqWidth, int reqHeight){
        //raw height and width of image
        final int width = bmOptions.outWidth;
        final int height = bmOptions.outHeight;
        int inSampleSize = 1;

        if (width>reqWidth|| height > reqHeight){
            final int halfHeight = height/2;
            final int halfWidth = width/2;

            while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight){
                inSampleSize *=2;
            }
        }

        // This offers some additional logic in case the image has a strange
        // aspect ratio. For example, a panorama may have a much larger
        // width than height. In these cases the total pixels might still
        // end up being too large to fit comfortably in memory, so we should
        // be more aggressive with sample down the image (=larger inSampleSize).

        long totalPixels = width * height / inSampleSize;

        // Anything more than 2x the requested pixels we'll sample down further
        final long totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels > totalReqPixelsCap) {
            inSampleSize *= 2;
            totalPixels /= 2;
        }

//        Log.e(TAG, "in sample size " + inSampleSize);
        return inSampleSize;
    }

    public static int calculateInSampleSize(BitmapFactory.Options bmOptions, int reqWidth){
        //raw height and width of image
        final int width = bmOptions.outWidth;
        int inSampleSize = 1;

        if (width>reqWidth){
            final int halfWidth = width/2;

            while ((halfWidth / inSampleSize) > reqWidth){
                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampleBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight){

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);// Calculate inSampleSize
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    public static int getRealWidthInPx(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return  (int) (metrics.widthPixels);
    }

    public static int getRealHeighInPx(Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//        float density = metrics.density;
        return metrics.heightPixels;
    }

    public static Bitmap createScaledBitmap(Bitmap bitmap, int outWidth){
//        Log.d(TAG, "before scale: "+ bitmap.getByteCount());
//        if (bitmap.getWidth() < outWidth)
//            return bitmap;
        int outHeight = (int) (bitmap.getHeight() * ((float)outWidth / bitmap.getWidth()));
        bitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);

//        Log.d(TAG, "after scale: "+ bitmap.getByteCount());
        return bitmap;
    }
}