package video.com.relavideolibrary.adapter;

import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;


import java.util.Collection;

import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.model.MusicCategoryBean;

/**
 * Created by chad
 * Time 17/8/3
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class MusicCategoryAdapter extends BaseRecyclerAdapter<MusicCategoryBean> {

    public MusicCategoryAdapter(@LayoutRes int layoutId, RecyclerView recyclerView, Collection<MusicCategoryBean> list) {
        super(layoutId, recyclerView, list);
        //默认第一个选中
        mData.get(0).selected = true;
    }

    @Override
    public void dataBinding(final BaseViewHolder holder, final MusicCategoryBean item, final int position) {
        final ImageView categoryIcon = holder.getView(R.id.category_icon);

        categoryIcon.setImageResource(item.selected ? item.selectImage : item.unSelectImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mData.size(); i++) {
                    if (i == position) item.selected = true;
                    else mData.get(i).selected = false;
                }
                notifyDataSetChanged();
                if (onItemClickListener != null) onItemClickListener.onItemClick(item, position);
            }
        });
        holder.setText(R.id.category_name, item.categoryName);

    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(MusicCategoryBean item, int position);
    }
}
