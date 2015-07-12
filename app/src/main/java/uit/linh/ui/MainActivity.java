package uit.linh.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;

import uit.linh.adapters.ViewPagerAdapter;
import uit.linh.debugs.Logger;
import uit.linh.utils.JsonReader;
import uit.linh.utils.NetworkUtil;
import uit.linh.widgets.SlidingTabLayout;


public class MainActivity extends AppCompatActivity implements
        RecentlyMemeFragment.OnFragmentInteractionListener {

    public static final String HOST = "http://meme.890m.com/";
    public static final String MEME_NEW_PATH = "meme-new/";
    public static final String MEME_ORIGIN_PATH = "meme-origin/";
    public static final String STAMPS_PATH = "stamps/";
    public static final String PACKAGE_jSON = "package_v2.json";
    public static final String MEME_STORE_DIR = "meme_creator/";

    private ViewPager viewPager;
    private LinearLayout toolbarContainer;
    private SlidingTabLayout tabs;
    private ViewPagerAdapter viewPagerAdapter;
    private JSONObject jMemes;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!NetworkUtil.checkConnection(getBaseContext())) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.error_title));
            builder.setMessage(getResources().getString(R.string.no_network_connection))
                    .setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finishApp();
                        }
                    }).create().show();
        }

        jMemes = new JSONObject();
        getJsonPackage();

        //== setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
//                    .addTestDevice("16A0CA736C2F597791762AD035007B3B")
                .build();
        mAdView.loadAd(adRequest);
    }

    /**
     * download the package that contains the images' url from host server.
     */
    private void getJsonPackage(){
        this.jMemes = new JSONObject();
        new JsonDownloadTask(this).execute(MainActivity.HOST + MainActivity.PACKAGE_jSON);
    }

    private void registerViewPager(){
        //== assigning view pager and setting the adapter
        String[] tabTitles = getResources().getStringArray(R.array.tab_tiles);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), tabTitles, jMemes);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //== assigning the tab layout view
        toolbarContainer = (LinearLayout) findViewById(R.id.toolbar_container);
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
//        tabs.setDistributeEvenly(true);//to make tabs fixed set this true, this makes the tabs space evenly in available width
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });
        tabs.setViewPager(viewPager);
//            Log.d("main", "tool");
    }

    private void finishApp(){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onFragmentInteraction(Uri uri) {

    }

    class JsonDownloadTask extends AsyncTask<String, Integer, JSONObject> {

        private static final String TAG = "JsonDownloadTask";
//        AsyncTaskInteractionListener asyncTaskInteractionListener;
        final ProgressDialog dialog;
        private IOException ex;
        private JSONException jEx;


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
        protected JSONObject doInBackground(String... strings){
            String url = strings[0];
            JSONObject jsonObject = null;
            try {
                try {
                    jsonObject = JsonReader.downloadJson(url);
                } catch (JSONException e) {
                    if (Logger.DEBUG_MODE)
                        e.printStackTrace();

                    this.jEx = e;
                    cancel(true);
                }
            }catch (ConnectException e){
                this.ex = e;
                if (Logger.DEBUG_MODE)
                    e.printStackTrace();
                cancel(true);
            }catch (final IOException e) {
                if (Logger.DEBUG_MODE)
                    e.printStackTrace();

                this.ex = e;
                cancel(true);
            }
            return jsonObject;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            dialog.cancel();
            if (jsonObject != null) {
                jMemes = jsonObject;
                registerViewPager();
            }
        }

        @Override
        protected void onCancelled(JSONObject jsonObject) {
            super.onCancelled(jsonObject);

            dialog.cancel();
            if (this.ex != null || this.jEx!= null) {//if there is an error.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getResources().getString(R.string.error_title));
                builder.setMessage(getResources().getString(R.string.error_network_connection));
                builder.setPositiveButton(getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        finishApp();
                    }
                });
                builder.create().show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}