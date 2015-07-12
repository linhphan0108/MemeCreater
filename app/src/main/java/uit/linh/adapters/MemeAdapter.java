package uit.linh.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

import uit.linh.ui.NewMemeFragment;
import uit.linh.ui.R;
import uit.linh.utils.ImageWorker;

/**
 *
 * Created by linh on 11/06/2015.
 */
public class MemeAdapter  extends BaseAdapter{
    private Context context;
    private int resource;
    private  ArrayList<HashMap<String, String>> arr;
    private ImageWorker imageWorker;

    public MemeAdapter(Context context, int resource, ArrayList<HashMap<String, String>> arr, ImageWorker imageWorker) {
        this.context = context;
        this.resource = resource;
        this.arr = arr;
        this.imageWorker =  imageWorker;
    }

    @Override
    public int getCount() {
        if (arr == null){
            return 0;
        }
        return arr.size();
    }

    @Override
    public Object getItem(int i) {
        arr.get(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = ((Activity) context).getLayoutInflater().inflate(resource, null);
            ImageView imgMeme = (ImageView) view.findViewById(R.id.img_meme);

            view.setTag(new Holder(imgMeme));
        }

        Holder holder = (Holder) view.getTag();
        HashMap<String, String> memeUrl = arr.get(i);
        String url = memeUrl.get(NewMemeFragment.MEME_DEMO_KEY);
        imageWorker.loadImage(url, holder.imageView);

        return view;
    }

    private class Holder{
        public final ImageView imageView;

        private Holder(ImageView img) {
            this.imageView = img;
        }
    }
}
