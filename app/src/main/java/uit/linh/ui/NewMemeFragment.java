package uit.linh.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import uit.linh.adapters.ItemHolder;
import uit.linh.adapters.MemeRecycleViewAdapter;
import uit.linh.utils.ImageFetcher;
import uit.linh.utils.ImageResizer;
import uit.linh.utils.MemoryCache;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link NewMemeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewMemeFragment extends android.support.v4.app.Fragment{

    private static final String TAG = "MemeDemoFragment";
    private static final String IMAGE_CACHE_DIR = "new";
    public static final String MEME_DEMO_KEY = "demo";
    public static final int IN_SAMPLE_SIZE = 4;

    private ArrayList<String> memeUrls = null;
    private MemeRecycleViewAdapter adapter;
    private ImageFetcher imageFetcher;
    
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MemeDemoFragment.
     * @param jString
     */
    public static NewMemeFragment newInstance(String jString) {
        NewMemeFragment fragment = new NewMemeFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", jString);
        fragment.setArguments(args);
        return fragment;
    }

    public NewMemeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                String jString = getArguments().getString("data");
                if (jString != null) {
                    JSONArray arr = new JSONArray(jString);
                    getMergerLink(arr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int imageThumbSize = ImageResizer.getRealWidthInPx(getActivity().getBaseContext()) /IN_SAMPLE_SIZE;
        MemoryCache.ImageCacheParams imageCacheParams = new MemoryCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
        imageCacheParams.setMemCacheSizePercent(0.25f);

        imageFetcher = new ImageFetcher(getActivity(), imageThumbSize);
        imageFetcher.setLoadingBitmap(R.drawable.meme_holder);
        imageFetcher.addImageCache(getActivity(), imageCacheParams);
        adapter = new MemeRecycleViewAdapter(getActivity(), R.layout.meme_image_item, memeUrls, imageFetcher, new ItemHolder.IViewHolderClick() {
            @Override
            public void OnImageClick(View caller, int position) {
                String memeUrl = memeUrls.get(position);
                Intent intent = new Intent(getActivity().getBaseContext(), Done.class);
                intent.putExtra("is cache", true);
                intent.putExtra("image path", memeUrl);
                startActivity(intent);
            }
        });
    }

    private void getMergerLink(JSONArray arr) throws JSONException {
        if (arr == null) return;
        memeUrls = new ArrayList<>();
        for (int i=0; i<arr.length(); i++){
            memeUrls.add(MainActivity.HOST + MainActivity.MEME_NEW_PATH + arr.getJSONObject(i).getString("url"));
        }
    }

    private void closeApp(){
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.e("bikini", "on resume");
        imageFetcher.setExitTasksEarly(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_meme, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getBaseContext(), 2));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            View toolbar = getActivity().findViewById(R.id.tool_bar);
            View toolbarContainer = getActivity().findViewById(R.id.toolbar_container);

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE){

                   if (Math.abs(toolbarContainer.getTranslationY()) > toolbar.getBottom() /2){
                       hideToolbar();
                   }else{
                       showToolbar();
                   }
                }

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING){
//                    Log.d(TAG, "SCROLL_STATE_FLING");
                    imageFetcher.setPauseWork(true);
                }else{
                    imageFetcher.setPauseWork(false);
                }
            }

            private void showToolbar() {
                toolbarContainer.animate().translationY(0);
            }

            private void hideToolbar() {
                toolbarContainer.animate().translationY(-toolbar.getBottom()).start();
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                dy = Math.round(dy/2);
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0){
                    hideToolbarBy(dy);

                }else{
                    showToolbarBy(dy);
                }
            }

            private void showToolbarBy(int dy) {
                if (canShowMore(dy)) {
                    toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
                }else{
                    toolbarContainer.setTranslationY(0);
                }
            }

            private void hideToolbarBy(int dy) {
                if (canHideMore(dy)) {
                    toolbarContainer.setTranslationY(toolbarContainer.getTranslationY() - dy);
                }else{
                    toolbarContainer.setTranslationY(-toolbar.getBottom());
                }
            }

            private boolean canShowMore(int dy){
                return toolbarContainer.getTranslationY() - dy < 0;
            }

            private boolean canHideMore(int dy){
//                Log.d(TAG, "dy: "+ dy);
//                Log.d(TAG, "getTranslationY " + toolbarContainer.getTranslationY());
//                Log.d(TAG, " =" + (toolbarContainer.getTranslationY() - dy));
                return Math.abs(toolbarContainer.getTranslationY() - dy) < toolbar.getBottom();
            }


        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.e("bikini", "on pause");
        imageFetcher.setPauseWork(false);
        imageFetcher.setExitTasksEarly(true);
        imageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imageFetcher.closeCache();
    }
}
