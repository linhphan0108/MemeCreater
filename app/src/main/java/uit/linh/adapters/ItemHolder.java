package uit.linh.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import uit.linh.ui.R;

/**
 *
 * Created by linh on 12/06/2015.
 */
public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public ImageView imageView;
    public IViewHolderClick listener;


    public ItemHolder(View itemView, IViewHolderClick listener) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.img_meme);
        this.listener = listener;
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view instanceof ImageView){
            listener.OnImageClick(view, this.getPosition());
        }
    }

    public interface IViewHolderClick{
        public void OnImageClick(View caller, int position);
    }
}
