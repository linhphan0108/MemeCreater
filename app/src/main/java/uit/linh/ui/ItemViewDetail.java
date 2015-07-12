package uit.linh.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.HashMap;

import uit.linh.utils.ImageResizer;
import uit.linh.utils.MemoryCache;

public class ItemViewDetail extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ItemViewDetail";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    public static final int IN_SAMPLE_SIZE = 1;

    private Toolbar toolbar;
    private ImageView imageView;
    private ImageButton btnEdit, btnFbShare;

    private CallbackManager callbackManager;
    private Bitmap bitmap;

    private HashMap<String, String> memeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view_detail);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        memeUrl = (HashMap<String, String>) bundle.getSerializable("selected item");


        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("16A0CA736C2F597791762AD035007B3B")
                .build();
        mAdView.loadAd(adRequest);


        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        imageView = (ImageView) findViewById(R.id.img_detail_2);
        btnEdit = (ImageButton) findViewById(R.id.btn_edit_image);
        btnFbShare = (ImageButton) findViewById(R.id.btn_facebook_share_image);

        bitmap = getBitmapFromCache();
        bitmap = scaleBitmap(bitmap);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }

        setSupportActionBar(toolbar);
        btnFbShare.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

    }


    private Bitmap getBitmapFromCache(){
        BitmapDrawable drawable;
        MemoryCache.ImageCacheParams imageCacheParams = new MemoryCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        imageCacheParams.setMemCacheSizePercent(0.25f);

        MemoryCache memoryCache = new MemoryCache(imageCacheParams);
        drawable = memoryCache.getBitmapFromMemoryCache(memeUrl.get(NewMemeFragment.MEME_DEMO_KEY));
        if (drawable != null){
            return drawable.getBitmap();
        }else {
            Bitmap bitmap = memoryCache.getBitmapFromDiskCache(memeUrl.get(NewMemeFragment.MEME_DEMO_KEY));
            if (bitmap != null){
                return bitmap;
            }
        }
        return null;
    }

    private Bitmap scaleBitmap(Bitmap bitmap){
        final int imageWidth = ImageResizer.getRealWidthInPx(this);
        return ImageResizer.createScaledBitmap(bitmap, imageWidth);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_view_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_edit_image:
                Bundle bundle = new Bundle();
                bundle.putSerializable("selected item", memeUrl);
                Intent intent = new Intent(getBaseContext(), MemeCreator.class);
                intent.putExtra("data", bundle);
                startActivity(intent);
                break;

            case R.id.btn_facebook_share_image:
//                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                if (bitmap != null) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.open_facebook), Toast.LENGTH_LONG).show();
                    sharePhotoToFacebook(bitmap);
                }else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.error_facebook_share), Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    private void sharePhotoToFacebook(String imagePath){
        File file = new File(imagePath);
        if (!file.exists()){
//            Log.d(TAG, "image doesn't exist");
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        sharePhotoToFacebook(bitmap);
    }

    private void sharePhotoToFacebook(Bitmap bitmap){
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .setCaption("Give me my codez or I will ... you know, do that thing you don't like!")
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        ShareDialog.show(this, content);
    }
}
