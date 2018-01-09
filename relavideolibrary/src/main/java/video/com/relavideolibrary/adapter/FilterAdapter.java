package video.com.relavideolibrary.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import java.util.Collection;

import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.FilterTransformation;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.FilterBean;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class FilterAdapter extends BaseRecyclerAdapter<FilterBean> {

    public FilterAdapter(int layoutId, RecyclerView recyclerView, Collection<FilterBean> list) {
        super(layoutId, recyclerView, list);

        //默认第一个选中
        mData.get(0).selected = true;
    }

    @Override
    public void dataBinding(final BaseViewHolder holder, final FilterBean item, final int position) {
        Glide.with(mContext)
                .load(VideoManager.getInstance().getVideoBean().videoPath)
                .signature(new StringSignature(String.valueOf(item.filterId)))
                .transform(new FilterTransformation(mContext, item.filterId))
                .into((ImageView) holder.getView(R.id.filter_image));

        final TextView filter_name = holder.getView(R.id.filter_name);
        filter_name.setTextColor(mContext.getResources().getColor(item.selected ? R.color.rela_color : R.color.black_gray));
        filter_name.setTypeface(Typeface.defaultFromStyle(item.selected ? Typeface.BOLD : Typeface.NORMAL));
        filter_name.setText(item.filterName);
        holder.getView(R.id.filter_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mData.size(); i++) {
                    if (i == position) {
                        item.selected = true;
                    } else {
                        mData.get(i).selected = false;
                    }
                }
                notifyDataSetChanged();
                if (onItemClickListener != null) onItemClickListener.itemClick(item.filterId);
            }
        });
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void itemClick(int filterId);
    }
}
