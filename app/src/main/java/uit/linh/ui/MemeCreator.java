package uit.linh.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import uit.linh.providers.Fonts;
import uit.linh.utils.ImageFetcher;
import uit.linh.utils.ImageResizer;
import uit.linh.utils.MemoryCache;


public class MemeCreator extends ActionBarActivity implements View.OnTouchListener, View.OnDragListener,
        GroupControlFragment.OnFragmentInteractionListener, EditTextFragment.OnFragmentInteractionListener{


    private static final String TAG = "MainActivity";
    public static final int MAX_FONT_SIZE = 200;//pixel unit
    public static final int DEFAULT_FONT_SIZE = 100;
    public static final int PROGRESS_BAR_STEP = 10;
    public static final int REQ_CODE_OPEN_FONT_PICKER = 1111;
    public static final String FLAG_FONT_CODE_RESULT = "selected font";
    private static final String CAPTION_PREFERENCE = "caption";
    private static final String SHARE_REFERENCE_FILE = "meme_creator";
    public static final String IMAGE_CACHE_DIR = "creator";
    public static final String FILE_NAME_ORIGIN = "origin";
    public static final String FILE_NAME_CREATED = "product";
    private static String homeAppPath;

    private Toolbar toolbar;
    FrameLayout vgImageContainer;
    LinearLayout fragment;
    ImageView imageView;
    TextView txtCaption1;

    private MemoryCache memoryCache;
    private String memeUrl;
    private int imageWidth;
    private boolean btnEnable = false;
    private  boolean isCreated = false;

    public static String defaultCaption;
    private String origin_url;
    private float currX = 0f;
    private float currY = 0f;
    private float coordX = 0f;//new coordination x
    private float coordY = 0f;
    float ratioY;
    private float marginX = 0f;//the margin of text view caption with container.
    private float marginY = 0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator);

        Intent intent = getIntent();
        memeUrl = intent.getStringExtra("image path");
        defaultCaption = getResources().getString(R.string.caption1);
        imageWidth = ImageResizer.getRealWidthInPx(this);
        homeAppPath = getRootPath(this);


        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("16A0CA736C2F597791762AD035007B3B")
                .build();
        mAdView.loadAd(adRequest);


        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        imageView = (ImageView) findViewById(R.id.imageView);
        txtCaption1 = (TextView) findViewById(R.id.txt_caption1);
        vgImageContainer = (FrameLayout) findViewById(R.id.vg_image_container);
        fragment = (LinearLayout) findViewById(R.id.fragment);
        txtCaption1.setVisibility(View.INVISIBLE);

        new GetBitmapAsync(this, memeUrl).execute();

        MemoryCache.ImageCacheParams imageCacheParams = new MemoryCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        imageCacheParams.setMemCacheSizePercent(0.25f);
        memoryCache = new MemoryCache(imageCacheParams);

        setSupportActionBar(toolbar);
        vgImageContainer.setOnDragListener(this);
        txtCaption1.setOnTouchListener(this);
        txtCaption1.setTypeface(Typeface.SERIF);
        txtCaption1.setTextSize(convertPixel2Sp(DEFAULT_FONT_SIZE, getBaseContext()));


        GroupControlFragment groupControlFragment = GroupControlFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment, groupControlFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = getSharedPreferences(SHARE_REFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CAPTION_PREFERENCE, "");
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_creator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up rectangle_button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement

        switch (item.getItemId()){
            case R.id.action_done:
                Bitmap bitmap;
                if (isCreated)
                     bitmap= getBitmapFromCache(FILE_NAME_CREATED);
                else bitmap = getBitmapFromCache(FILE_NAME_ORIGIN);
                if (txtCaption1.getText().length() > 0) {
                    bitmap = processBitmap(bitmap);
                }
                String path = saveBitmap(this, bitmap, MemoryCache.generateHash(origin_url));

                if (path != null) {
                    Intent intent = new Intent(this, Done.class);
                    intent.putExtra("image path", path);
                    startActivity(intent);
                }

//                imageView.setImageBitmap(bitmap);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_CODE_OPEN_FONT_PICKER:
                if (resultCode != RESULT_OK)
                    return;
                int i = data.getIntExtra(FLAG_FONT_CODE_RESULT, 0);
                txtCaption1.setTypeface(Typeface.createFromAsset(getAssets(), Fonts.fonts[i]));
                break;
        }
    }

    private boolean CacheBitmap(Bitmap bitmap, String fileName) throws IOException {
        File file = new File(getCacheDir(), fileName);
        if (file.exists()) {
            if (file.delete());
//                Log.d(TAG, "cached image deleted");
        }
        OutputStream out = new FileOutputStream(file);
        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        bitmap.compress(compressFormat, 100, out);
        out.flush();
        out.close();

        return true;
    }

    private boolean removeCache(String fileName){
        File file = new File(getCacheDir(), fileName);
        if (file.exists()) {
            if (file.delete()) {
//                Log.d(TAG, "deleted cache successfully");
            }
            return true;
        }
        return false;
    }

    private Bitmap getBitmapFromCache(String fileName){
        Bitmap bitmap = null;
        File file = new File(getCacheDir(), fileName);
        if (file.exists()){
            InputStream in;
            try {
                in = new FileInputStream(file);
                bitmap = BitmapFactory.decodeStream(in);
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        return bitmap;
    }


    private Bitmap processBitmap(Bitmap bitmap){
        Bitmap newBitmap;

        if (bitmap == null) {
            Toast.makeText(this, "can't get the selected image", Toast.LENGTH_SHORT).show();
            return null;
        }
        Bitmap.Config config = bitmap.getConfig();
        if (config != null){
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), config);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        String caption = txtCaption1.getText().toString();
//        edtCaption.setText("");
        if (caption != null){

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(txtCaption1.getCurrentTextColor());
            paint.setTypeface(txtCaption1.getTypeface());
            paint.setTextSize(txtCaption1.getTextSize());
            paint.setStyle(Paint.Style.STROKE);
            Rect rectangleBounds = new Rect();
            paint.getTextBounds(caption, 0, caption.length(), rectangleBounds);

//            ratioX = (float)imageView.getWidth() / bitmap.getWidth();
            ratioY = (float)bitmap.getHeight()/imageView.getHeight();
            marginY = (imageView.getHeight() - bitmap.getHeight())/2;

//            int bitmapWidth = bm1.getWidth();//1000 x 968 => 720 x 696 =>  x 185
//            int imageviewWidth = imageView.getWidth();//720 x 784
//            scale = calculateScaleRatio(imageView, bitmap);
            float textViewPadding = convertDP2Pixel(6, getBaseContext());//dp unit
            coordX = txtCaption1.getX() + marginX + textViewPadding;
            coordY = (txtCaption1.getY() - marginY + textViewPadding + rectangleBounds.height());

            int widthLine = vgImageContainer.getWidth();
            ArrayList<String> lines = breakLines(caption, paint, widthLine, coordX, 0f);

            if (lines != null) {
                paint.setTextSize(txtCaption1.getTextSize());//sets text size again.
                drawTexts(canvas, paint, lines, rectangleBounds.height());
            }
//            else Log.d(TAG, "lines is null");

        }else{
            Toast.makeText(getBaseContext(), "caption empty!", Toast.LENGTH_SHORT).show();
        }
        return newBitmap;
    }

    public static String saveBitmap(Context context, Bitmap bitmap, String fileName){
        String path = "";
        if (bitmap != null){
            try {
                String basePath = homeAppPath + File.separator + MainActivity.MEME_STORE_DIR;
                File file = new File(basePath);
                if (!file.exists()){
                    if (file.mkdirs()){
//                        Log.d(TAG, "created storage successfully");
                    }else{
                        Toast.makeText(context, "Sorry, we can't access your sdcard", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }

                path = basePath + fileName;
                file = new File(path);
                if(file.exists()){
                    Toast.makeText(context, "file exists", Toast.LENGTH_SHORT).show();
                    if (file.delete()){
                        Toast.makeText(context, "image deleted", Toast.LENGTH_SHORT).show();
                    }
                }
                OutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(context, path, Toast.LENGTH_SHORT).show();
            return path;
        }
        return null;
    }

    private ArrayList<String> breakLines(String string, Paint paint, int widthLine, float coordX, float textViewPadding){
        ArrayList<String> lines = new ArrayList<>();
        float textWidth =  paint.measureText(string) + coordX +textViewPadding;
        if (textWidth > widthLine){//if the length of the string is longer than the width of container then breaking to multi lines
            String[] array = string.split(" ");
            String line = "";
            for (int i=0; i<array.length; i++){
                if (line.length() == 0)
                    line += array[i];
                else line += " "+ array[i];
                if (paint.measureText(line) + coordX + textViewPadding < widthLine){
                    if (i == array.length-1)//if there is the last word in the string.
                        lines.add(line);
                }else{// breaks new line
                    if (line.length() > array[i].length()) {
                        lines.add(line.substring(0, line.length() - array[i].length() - 1));
                        i--;
                    }
                    else //if line has only one word then adding line to lines.
                        lines.add(line);

                    line = "";
                }

            }
        }else{//if the length of string is smaller the width of container.
            lines.add(string);
        }
        return lines;
    }

    private void drawTexts(Canvas canvas, Paint paint, ArrayList<String> lines, float lineHeight){
        float x = coordX;
        float y = coordY;

        for (int i=0; i<lines.size(); i++){
            String line = lines.get(i);
            if (i > 0)
                y += lineHeight + lineHeight/4;
            canvas.drawText(line, x, y, paint);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                currX = motionEvent.getX();
                currY = motionEvent.getY();
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(null, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
        }
        return false;
    }

    @Override
    public boolean onDrag(View containerView, DragEvent dragEvent) {
        View child = (View) dragEvent.getLocalState();
        switch (dragEvent.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
//                Log.d(TAG, "Drag event started");
//                Log.e(TAG, "x: "+ child.getX() + " - Y: "+ child.getY());
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
//                Log.d(TAG, "Drag event entered into "+ containerView.toString());
//                Log.e(TAG, "x: "+ child.getX() + " - Y: "+ child.getY());
                break;
            case DragEvent.ACTION_DRAG_EXITED:
//                Log.d(TAG, "Drag event exited from "+ containerView.toString());
//                Log.e(TAG, "x: "+ child.getX() + " - Y: "+ child.getY());
                break;

            case DragEvent.ACTION_DROP:
//                Log.e(TAG, "Dropped");

                ViewGroup owner = (ViewGroup) child.getParent();
                owner.removeView(child);
                FrameLayout container = (FrameLayout) containerView;
                container.addView(child);

                int childWidth = child.getWidth();
                int childHeight = child.getHeight();
                int parentWidth = container.getWidth();
                int parentHeight = container.getHeight();
                float dragX = dragEvent.getX();
                float dragY = dragEvent.getY();
//                Log.e(TAG, "x: "+ dragX + " % Y: "+ dragY);

                //== setting coordination x
                if (dragX < childWidth/4) {
                    dragX = 0;
                }else if(dragX + childWidth/4 > parentWidth){
                    dragX = parentWidth -childWidth;
                }else{
                    dragX = dragX - childWidth/2;
                }
                //setting coordination y
                if (dragY < marginY) {
                    dragY = marginY;
                }else if(dragY + childHeight/4 > parentHeight){
                    dragY = parentHeight - childHeight;
                }else{
                    dragY = dragY - childHeight/2;
                }

                child.setX(dragX);
                child.setY(dragY);
                coordX = dragX;
                coordY = dragY;

                child.setVisibility(View.VISIBLE);
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
//                Log.d(TAG, "Drag ended");
//                edtCaption.setEnabled(true);
                if (dragEvent.getResult()){
//                    Toast.makeText(MainActivity.this, "The drop was handled.", Toast.LENGTH_LONG).show();
                    return true;

                }else{
                    Toast.makeText(MemeCreator.this, "The drop didn't work.",
                            Toast.LENGTH_LONG).show();
                    child.setX(currX);
                    child.setY(currY);
                    child.setVisibility(View.VISIBLE);
                    return false;
                }

            default:
                break;
        }
        return true;
    }

    //=== group control fragment
    @Override
    public void replaceFragment() {
        EditTextFragment editTextFragment = EditTextFragment.newInstance((int) txtCaption1.getTextSize());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, editTextFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean isButtonEnabled() {
        return btnEnable;
    }

    private void setButtonEnable(boolean value){
        btnEnable = value;
    }

    @Override
    public void setCaption(String caption) {
        txtCaption1.setText(caption);
    }

    @Override
    public void createNewCaption() {
        Bitmap cachedBitmap;
        try {
            if(isCreated) {
                cachedBitmap = getBitmapFromCache(FILE_NAME_CREATED);
            }else{
                cachedBitmap = getBitmapFromCache(FILE_NAME_ORIGIN);
            }

            if (cachedBitmap == null) {
//                Log.e(TAG, "createNewCaption - can't get the cached bitmap");
                return;
            }

            Bitmap bitmap = processBitmap(cachedBitmap);
            CacheBitmap(bitmap, FILE_NAME_CREATED);
            imageView.setImageBitmap(bitmap);
            txtCaption1.setText(defaultCaption);
            isCreated = true;

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {
        removeCache(FILE_NAME_CREATED);
        imageView.setImageBitmap(getBitmapFromCache(FILE_NAME_ORIGIN));
        txtCaption1.setText(defaultCaption);
        isCreated = false;
    }

    //=== edit text fragment
    @Override
    public void setFontSize(int fontSize) {
        txtCaption1.setTextSize(convertPixel2Sp(fontSize, getBaseContext()));
    }

    @Override
    public void setTextColor(int color) {
        txtCaption1.setTextColor(color);
    }

    public static float convertPixels2Dp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / metrics.densityDpi;
    }

    public static float convertDP2Pixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    public static float convertSp2Pixel(float sp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return sp * (metrics.scaledDensity / 160f);
    }

    public static float convertPixel2Sp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / metrics.scaledDensity;
    }

    public static String getRootPath(Context context){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.isExternalStorageRemovable() ? Environment.getExternalStorageDirectory().getAbsolutePath()
                : context.getFilesDir().getAbsolutePath();
    }

    private class GetBitmapAsync extends AsyncTask<Void, Void, Bitmap>{
        private final String url;
        final ProgressDialog dialog;
        private final Context context;
        private IOException exception;

        private GetBitmapAsync(Context context, String url) {
            this.context = context;
            this.url = url;
            dialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setImageResource(R.drawable.meme_holder);
            dialog.setCancelable(true);
            dialog.setMessage(context.getResources().getString(R.string.loading_image));
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
//            imageView.setImageResource(R.drawable.meme_holder);
            Bitmap bitmap;
            try {
                bitmap = ImageFetcher.downloadBitmap(url);
            } catch (IOException e) {
                exception = e;
                e.printStackTrace();
                return null;
            }
            return ImageResizer.createScaledBitmap(bitmap, imageWidth);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            dialog.dismiss();
            if (exception != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getResources().getString(R.string.error_title));
                builder.setMessage(getResources().getString(R.string.error_network_connection));
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        cancel(true);
                        finish();
                    }
                });
                builder.create().show();
            }else {

                imageView.setImageBitmap(bitmap);
                txtCaption1.setVisibility(View.VISIBLE);
                setButtonEnable(true);

                try {
                    CacheBitmap(bitmap, FILE_NAME_ORIGIN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cancel(true);
            }
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
