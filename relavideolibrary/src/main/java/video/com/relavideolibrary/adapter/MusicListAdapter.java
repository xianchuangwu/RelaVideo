package video.com.relavideolibrary.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Collection;

import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.DensityUtils;
import video.com.relavideolibrary.model.MusicBean;
import video.com.relavideolibrary.surface.MusicActivity;
import video.com.relavideolibrary.view.ShaderView;

/**
 * Created by chad
 * Time 17/8/3
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class MusicListAdapter extends BaseRecyclerAdapter<MusicBean> {

    private MusicActivity activity;

    private boolean categoryVisible = true;

    public MusicListAdapter(@LayoutRes int layoutId, final RecyclerView recyclerView, Collection<MusicBean> list) {
        super(layoutId, recyclerView, list);

        this.activity = (MusicActivity) recyclerView.getContext();
    }

    @Override
    public void dataBinding(final BaseViewHolder holder, final MusicBean item, final int position) {

        holder.setVisible(R.id.music_category_container, categoryVisible);

        String time = DensityUtils.longToChronometer(item.musicHours * 1000);
        holder.setText(R.id.music_name, item.name)
                .setText(R.id.music_autor, item.singer)
                .setText(R.id.music_time, time)
                .setText(R.id.music_category, item.categoryName);

        RelativeLayout itemview = holder.getView(R.id.item);
        final ImageView playIcon = holder.getView(R.id.play_icon);
        final LottieAnimationView playAnim = holder.getView(R.id.play_anim);
        ShaderView progress = holder.getView(R.id.progress);
        LottieAnimationView loading = holder.getView(R.id.loading);
        loading.setVisibility(View.INVISIBLE);
        final MusicBean musicBean = activity.getCurrentMusic();
        if (musicBean != null && item.musicId == musicBean.musicId) {
            if (!musicBean.isPause) {
                playIcon.setVisibility(View.INVISIBLE);
                playAnim.setVisibility(View.VISIBLE);
                playAnim.setProgress(0);
                playAnim.playAnimation();
            } else {
                playIcon.setVisibility(View.VISIBLE);
                playAnim.setVisibility(View.INVISIBLE);
                if (playAnim.isAnimating()) playAnim.cancelAnimation();
            }
            progress.setVisibility(View.VISIBLE);
            progress.setProgress(activity.getCurrentProgress(), mData.get(position).musicHours * 1000 / 100);
            itemview.setBackgroundColor(mContext.getResources().getColor(R.color.click_feedback));
        } else {
            playIcon.setVisibility(View.VISIBLE);
            playAnim.setVisibility(View.INVISIBLE);
            if (playAnim.isAnimating()) playAnim.cancelAnimation();
            progress.setVisibility(View.INVISIBLE);
            itemview.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder, item, position);
                }
            }
        });
    }

    public void setCategoryVisible(boolean categoryVisible) {
        this.categoryVisible = categoryVisible;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(BaseViewHolder holder, MusicBean item, int position);
    }
}
