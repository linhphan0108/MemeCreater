package uit.linh.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

import uit.linh.ui.NewMemeFragment;
import uit.linh.utils.ImageFetcher;
import uit.linh.utils.ImageWorker;

/**
 *
 * Created by linh on 12/06/2015.
 */
public class MemeRecycleViewAdapter extends RecyclerView.Adapter<ItemHolder>{
    private Context context;
    private int resource;
    private ArrayList<String> arr;
    private ImageFetcher imageFetcher;
    private ItemHolder.IViewHolderClick listener;

    public MemeRecycleViewAdapter(Context context, int resource, ArrayList<String> arr, ImageFetcher imageFetcher, ItemHolder.IViewHolderClick listener) {
        this.context = context;
        this.resource = resource;
        this.listener = listener;
        this.arr = arr;
        this.imageFetcher = imageFetcher;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup, false);
        return new ItemHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int i) {
        String url = arr.get(i);
        imageFetcher.loadImage(url, holder.imageView);
    }

    @Override
    public int getItemCount() {
        if (arr != null)
            return arr.size();
        return 0;
    }
}