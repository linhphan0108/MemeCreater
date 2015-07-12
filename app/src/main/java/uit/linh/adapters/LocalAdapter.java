package uit.linh.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import uit.linh.ui.RecentlyMemeFragment;
import uit.linh.utils.ImageResizer;

/**
 *this loads created images from storage.
 * Created by linh on 13/06/2015.
 */
public class LocalAdapter extends RecyclerView.Adapter<RecentlyHolder> {
    private Context context;
    private int resource;
    private ArrayList<File> arr;
    private RecentlyHolder.IViewHolderClick listener;
    private RecentlyMemeFragment.LocalImageWorker localImageWorker;

    public LocalAdapter(Context context, int resource, ArrayList<File> arr,
                        RecentlyMemeFragment.LocalImageWorker localImageWorker,
                        RecentlyHolder.IViewHolderClick listener) {
        this.context = context;
        this.resource = resource;
        this.localImageWorker = localImageWorker;
        this.listener = listener;
        this.arr = arr;
    }

    @Override
    public RecentlyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup, false);
        return new RecentlyHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(RecentlyHolder holder, int i) {
        localImageWorker.load(arr.get(i).getAbsolutePath(), holder.imageView);//830042416264
    }

    @Override
    public int getItemCount() {
        return arr.size();
    }
}
