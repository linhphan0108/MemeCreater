package uit.linh.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import uit.linh.ui.R;

/**
 *
 * Created by linh on 13/06/2015.
 */
public class RecentlyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView imageView;
    public ImageButton btnDelete;
    public IViewHolderClick listener;


    public RecentlyHolder(View itemView, IViewHolderClick listener) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.img_meme);
        btnDelete = (ImageButton) itemView.findViewById(R.id.btn_delete);
        this.listener = listener;
        imageView.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view instanceof  ImageButton){
            listener.OnButtonClick(view, this.getPosition());

        }else if(view instanceof ImageView){
            listener.OnImageClick(view, this.getPosition());
        }
    }

    public interface IViewHolderClick{
        public void OnImageClick(View caller, int position);
        public void OnButtonClick(View caller, int position);
    }
}
