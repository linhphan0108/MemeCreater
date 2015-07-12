package uit.linh.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import uit.linh.utils.ImageFetcher;
import uit.linh.utils.ImageResizer;

public class Done extends AppCompatActivity {

    private static final String TAG = "FBShare";
    private Toolbar toolbar;
    private ImageView imageView;
    private CallbackManager callbackManager;
    private String imagePath = "";
    private ImageFetcher imageFetcher;
    public static final int IN_SAMPLE_SIZE = 1;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("16A0CA736C2F597791762AD035007B3B")
                .build();
        mAdView.loadAd(adRequest);

        imagePath= getIntent().getStringExtra("image path");
        if (getIntent().getBooleanExtra("is cache", false)) {
            int imageThumbSize = ImageResizer.getRealWidthInPx(getBaseContext()) / IN_SAMPLE_SIZE;
            imageFetcher = new ImageFetcher(this, imageThumbSize);
            imageFetcher.initHttpDiskCache();
            bitmap = imageFetcher.processBitmap(imagePath);
        }
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        imageView = (ImageView) findViewById(R.id.imageView);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else if (imagePath != null) {
            imageView.setImageURI(Uri.parse(imagePath));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fbshare, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up rectangle_button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()){
            case R.id.action_facebook_share:
                Toast.makeText(getBaseContext(), "Open facebook app! please waiting", Toast.LENGTH_SHORT).show();
                sharePhotoToFacebook();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        callbackManager.onActivityResult(requestCode, responseCode, data);
    }

    private void sharePhotoToFacebook(){
        if (bitmap == null) {
            File file = new File(imagePath);
            if (!file.exists()) {
//            Log.d(TAG, "image doesn't exist");
                return;
            }
            bitmap = BitmapFactory.decodeFile(imagePath);
        }
//        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.doremon);
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
