package uit.linh.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import uit.linh.adapters.LocalAdapter;
import uit.linh.adapters.RecentlyHolder;
import uit.linh.utils.ImageResizer;
import uit.linh.utils.ImageWorker;
import uit.linh.utils.RecentlyLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecentlyMemeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentlyMemeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentlyMemeFragment extends android.support.v4.app.Fragment {

    private static final String DISK_CACHE_SUBDIR = "meme_creator";

    private OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private TextView txtEmptyView;
    private ArrayList<File> memes;
    private LocalAdapter adapter;
    private String basePath;
    private int imageWidth;
    LocalImageWorker localImageWorker;
    private Bitmap bitmapLoading;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment DownloadedMemeFragment.
     */
    public static RecentlyMemeFragment newInstance() {
        RecentlyMemeFragment fragment = new RecentlyMemeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public RecentlyMemeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        basePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + MainActivity.MEME_STORE_DIR;

        imageWidth = ImageResizer.getRealWidthInPx(getActivity())/4;
        localImageWorker = new LocalImageWorker(getActivity());
        bitmapLoading = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.meme_holder);

//        memes = new ArrayList<>();
        memes = RecentlyLoader.getFiles(new File(basePath));
        adapter = new LocalAdapter(getActivity(), R.layout.recently_meme_image_item, memes, localImageWorker, new RecentlyHolder.IViewHolderClick() {
            @Override
            public void OnImageClick(View caller, int position) {
                Intent intent = new Intent(getActivity().getBaseContext(), Done.class);
                intent.putExtra("image path", memes.get(position).getPath());
                startActivity(intent);
            }

            @Override
            public void OnButtonClick(View caller, final int position) {
                final File deletedFile = memes.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("are you sure deleting this image");
                builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (deletedFile.delete()){
                            memes.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(getActivity().getBaseContext(), "your image was deleted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity().getBaseContext(), "can't delete your image", Toast.LENGTH_SHORT).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create().show();
            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();
//        memes = RecentlyLoader.getFiles(new File(basePath));
        notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recently_meme, container, false);

        txtEmptyView = (TextView) view.findViewById(R.id.txt_empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.recently_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity().getBaseContext(), 2));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            View toolbar = getActivity().findViewById(R.id.tool_bar);
            View toolbarContainer = getActivity().findViewById(R.id.toolbar_container);

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING){
                    localImageWorker.setPause(true);
                }else{
                    localImageWorker.setPause(false);
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    if (Math.abs(toolbarContainer.getTranslationY()) > toolbar.getBottom() /2){
                        hideToolbar();
                    }else{
                        showToolbar();
                    }
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

    private void notifyDataSetChanged(){
        if (adapter != null)
            adapter.notifyDataSetChanged();
        if (memes == null || memes.size() <= 0){
            txtEmptyView.setVisibility(View.VISIBLE);
        }else{
            txtEmptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }


    public class LocalImageWorker{
        private static final String TAG = "LocalImageWorker";
        private final Object loadImageLock = new Object();
        private boolean isPaused = false;
        private final Context context;

        public LocalImageWorker(Context context) {
//            loadImageLock.notifyAll();
            this.context = context;
        }

        public void load(String url, ImageView imageView){
            if (cancelPotentialWork(url, imageView)){
//                Bitmap bitmapLoading = BitmapFactory.decodeResource(context.getResources(), R.drawable.meme_holder);
                LoadImageAsyncTask loadImageAsync = new LoadImageAsyncTask(url, imageView);
                AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), bitmapLoading, loadImageAsync);
                imageView.setImageDrawable(asyncDrawable);
                loadImageAsync.execute();
            }
        }


        public boolean cancelPotentialWork(Object data, ImageView imageView){
            final LoadImageAsyncTask loadImageAsync = getBitmapWorkerTask(imageView);
            if (loadImageAsync != null){
                final String bitmapData = loadImageAsync.url;
                // If bitmapData is not yet set or it differs from the new data
                if (bitmapData == null || !bitmapData.equals(data)){
                    // Cancel previous task
                    //the cancel method of the AsyncTask class to stop the download in progress.
                    // It returns true most of the time, so that the download can be started in download.
                    loadImageAsync.cancel(true);
                }else{
                    // The same work is already in progress
                    // The same URL is already being downloaded.
                    return false;
                }
            }
            return true;
        }

        private LoadImageAsyncTask getBitmapWorkerTask(ImageView imageView){
            if (imageView != null){
                final Drawable drawable = imageView.getDrawable();
                if (drawable instanceof ImageWorker.AsyncDrawable){
                    final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                    return asyncDrawable.getBitmapWorkerTask();
                }
            }
            return null;
        }


        public void setPause(boolean value){
            synchronized (loadImageLock){
                isPaused = value;
                if (!isPaused){
                    loadImageLock.notifyAll();
                }
            }
        }

        public class LoadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
            private static final String TAG = "LoadImageAsync";
            private final String url;
            private final ImageView imageView;

            public LoadImageAsyncTask(String url, ImageView imageView) {
                this.url = url;
                this.imageView = imageView;
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {

                InputStream inputStream = null;
                synchronized (loadImageLock) {
                    while (isPaused){
                        try {
                            loadImageLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
//                        Log.d(TAG, "load image from sdcard from background");
                        inputStream = new FileInputStream(url);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(inputStream, null, options);
                        options.inSampleSize = ImageResizer.calculateInSampleSize(options, imageWidth);
                        options.inJustDecodeBounds = false;
                        return BitmapFactory.decodeStream(new FileInputStream(url), null, options);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (imageView != null){
                    imageView.setImageBitmap(bitmap);
//                    Log.e(TAG, "image is loaded");
                }else{
//                    Log.e(TAG, "image view is null");
                }
            }

//            @Override
//            protected void onCancelled() {
//                super.onCancelled();
//                synchronized (loadImageLock) {
//                    Log.e(TAG, "on cancelled");
//                    loadImageLock.notifyAll();
//                }
//            }

            @Override
            protected void onCancelled(Bitmap bitmap) {
                super.onCancelled(bitmap);
                synchronized (loadImageLock) {
//                    Log.e(TAG, "on cancelled");
                    loadImageLock.notifyAll();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                synchronized (loadImageLock) {
//                    Log.e(TAG, "on cancelled");
                    loadImageLock.notifyAll();
                }
            }
        }

        public class AsyncDrawable extends BitmapDrawable {
            final private WeakReference<LoadImageAsyncTask> bitmapWorkerTaskWeakReference;


            public AsyncDrawable(Resources res, Bitmap bitmap, LoadImageAsyncTask loadImageAsyncTask) {
                super(res, bitmap);
                this.bitmapWorkerTaskWeakReference = new WeakReference<>(loadImageAsyncTask);

            }

            public LoadImageAsyncTask getBitmapWorkerTask(){
                return bitmapWorkerTaskWeakReference.get();
            }
        }
    }
}
