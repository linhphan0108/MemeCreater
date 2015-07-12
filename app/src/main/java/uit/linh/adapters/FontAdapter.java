package uit.linh.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import uit.linh.ui.R;

/**
 *
 * Created by linh on 09/06/2015.
 */
public class FontAdapter extends BaseAdapter{
    private Context context;
    private int resource;
    private String[] arr;

    public FontAdapter(Context context, int resource, String[] arr) {
        this.context = context;
        this.resource = resource;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        return arr.length;
    }

    @Override
    public Object getItem(int i) {
        return arr[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null)
            view = ((Activity) context).getLayoutInflater().inflate(resource, null);
        TextView txtFontItem = (TextView) view.findViewById(R.id.txt_font_item);
        txtFontItem.setTypeface(Typeface.createFromAsset(context.getAssets(), arr[i]));
        txtFontItem.setText(arr[i].substring(0, arr[i].length()-3));
        return view;
    }
}
