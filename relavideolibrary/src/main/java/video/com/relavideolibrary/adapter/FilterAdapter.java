package video.com.relavideolibrary.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collection;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.FilterTransformation;
import video.com.relavideolibrary.filter.GPUImageExtTexFilter;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.surface.EditActivity;
import video.com.relavideolibrary.view.RoundCornersImageView;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class FilterAdapter extends BaseRecyclerAdapter<FilterBean> {

//    private final Bitmap bitmapWithFilterApplied;

    public FilterAdapter(int layoutId, RecyclerView recyclerView, Collection<FilterBean> list) {
        super(layoutId, recyclerView, list);

//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(VideoManager.getInstance().getVideoBean().videoPath);
//        Bitmap frameAtTime = retriever.getFrameAtTime();
//
//        GPUImage gpuImage = new GPUImage(mContext);
//        gpuImage.setImage(frameAtTime);
//        gpuImage.setFilter(new GPUImageSobelEdgeDetection());
//        bitmapWithFilterApplied = gpuImage.getBitmapWithFilterApplied();
    }

    @Override
    public void dataBinding(final BaseViewHolder holder, final FilterBean item, int position) {
        Glide.with(mContext)
                .load(VideoManager.getInstance().getVideoBean().videoPath)
                .asBitmap()
                .transform(new FilterTransformation(mContext, item.filterId))
                .into((RoundCornersImageView) holder.getView(R.id.filter_image));

//        ((RoundCornersImageView) holder.getView(R.id.filter_image)).setImageBitmap(bitmapWithFilterApplied);

        final TextView filter_name = holder.getView(R.id.filter_name);
        filter_name.setText(item.filterName);
        holder.getView(R.id.filter_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                filter_name.setTextColor();
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
