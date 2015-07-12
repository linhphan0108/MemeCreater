package uit.linh.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import uit.linh.providers.Colors;
import uit.linh.ui.R;

/**
 *
 * Created by linh on 09/06/2015.
 */
public class ColorAdapter extends BaseAdapter {
    private Context context;
    private int resource;
    private ArrayList<Colors.Color> arr;

    public ColorAdapter(Context context, int resource, ArrayList<Colors.Color> arr) {
        this.context = context;
        this.resource = resource;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        return arr.size();
    }

    @Override
    public Object getItem(int i) {
        return arr.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = ((Activity) context).getLayoutInflater().inflate(resource, null);
        ImageView btnColorItem = (ImageView) view.findViewById(R.id.btn_color_item);
        ImageView imgChecked = (ImageView) view.findViewById(R.id.img_checked);
        Colors.Color color = arr.get(i);
        if(i == 0){
            if (color.isChecked()) {
                imgChecked.setImageResource(R.drawable.ic_black_checked);
                imgChecked.setVisibility(View.VISIBLE);
            }else {
                imgChecked.setVisibility(View.INVISIBLE);
            }
        }else {
            btnColorItem.getBackground().setColorFilter(new LightingColorFilter(0x000000, Color.parseColor(color.getColor())));
            if (color.isChecked()) {
                imgChecked.setVisibility(View.VISIBLE);
            }else {
                imgChecked.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }
}
