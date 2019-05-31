package video.com.relavideolibrary.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.Collection;

import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.CenterCropRoundCornerTransform;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.Utils.DensityUtils;
import video.com.relavideolibrary.Utils.ScreenUtils;
import video.com.relavideolibrary.model.MediaModel;
import video.com.relavideolibrary.surface.RecordingActivity;

/**
 * Created by chad
 * Time 17/12/8
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class GalleryVideoAdapter extends BaseRecyclerAdapter<MediaModel> {

    private int mScreenWidth;
    private final BitmapFactory.Options options;
    private boolean hasSelect = false;

    public GalleryVideoAdapter(@LayoutRes int layoutId, RecyclerView recyclerView, Collection<MediaModel> list) {
        super(layoutId, recyclerView, list);
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    @Override
    public void dataBinding(BaseViewHolder holder, final MediaModel item, final int position) {

        holder.getView(R.id.take).setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        if (hasSelect) {
            if (item.status) {
                holder.itemView.setAlpha(1f);
                holder.itemView.setEnabled(true);
            } else {
                holder.itemView.setAlpha(0.5f);
                holder.itemView.setEnabled(false);
            }
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setEnabled(true);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                    ((Activity) mContext).startActivityForResult(new Intent(mContext, RecordingActivity.class), Constant.IntentCode.REQUEST_CODE_RECORDING);
                    return;
                }
                long duration = 0;
                if (!item.status) {
                    File file = new File(item.url);
                    if (!file.exists()) {
                        Toast.makeText(mContext, mContext.getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        //MediaExtractor在有的视频拿不到的duration。。。
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(item.url);
                        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        duration = Long.parseLong(durationStr);
                        retriever.release();
                        Log.i("MediaGalleryActivity", "url :" + item.url
//                                + "\nwidth :" + widthStr
//                                + "\nheight :" + heightStr
//                                        + "\naudioTrack :" + audioTrack
//                                        + "\naudioChannelCount :" + audioChannelCount
//                                        + "\nduration :" + duration
                                        + "\nduration :" + durationStr
//                                        + "\nsampleRate :" + sampleRate
                        );
//                        int height = 0;
//                        int width = 0;
//                        if (!TextUtils.isEmpty(heightStr)) height = Integer.parseInt(heightStr);
//                        if (!TextUtils.isEmpty(widthStr)) width = Integer.parseInt(widthStr);

//                        int bigBorder;
//                        int smallBorder;
//                        if (width >= height) {
//                            bigBorder = width;
//                            smallBorder = height;
//                        } else {
//                            bigBorder = height;
//                            smallBorder = width;
//                        }

//                        if (!(bigBorder / 16 == smallBorder / 9 && bigBorder % 16 == 0 && smallBorder % 9 == 0)) {
//                            Toast.makeText(mContext, mContext.getString(R.string.video_format_not_support), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        /*if (audioTrack == -1) {//无声
                            Toast.makeText(mContext, "暂不支持无声视频!", Toast.LENGTH_SHORT).show();
                            return;
                        } else*/
                        /*if (audioChannelCount == 1) {
                            Toast.makeText(mContext, "暂不支持单声道视频!", Toast.LENGTH_SHORT).show();
                            return;
                        }*/ /*else if (sampleRate != Constant.EncodeConfig.OUTPUT_AUDIO_SAMPLE_RATE_HZ) {
                            Toast.makeText(mContext, "暂不支持非44100采样率视频!", Toast.LENGTH_SHORT).show();
                            return;
                        } */
                        /*if (duration > Constant.VideoConfig.MAX_VIDEO_DURATION) {
                            Toast.makeText(mContext, mContext.getString(R.string.video_max_duration), Toast.LENGTH_SHORT).show();
                            return;
                        } else*/
                        if (duration < Constant.VideoConfig.MIN_VIDEO_DURATION) {
                            Toast.makeText(mContext, mContext.getString(R.string.video_min_duration), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, mContext.getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (!item.status) {//未选中
                    for (int i = 0; i < mData.size(); i++) {
                        if (i != position)
                            mData.get(i).status = false;
                        else {
                            mData.get(i).status = true;
                            hasSelect = true;
                        }
                    }
                    notifyDataSetChanged();
                    if (selectCallback != null) selectCallback.selected(item.url, duration);
                } else {
                    hasSelect = false;
                    item.status = !item.status;
                    notifyDataSetChanged();
                    if (selectCallback != null) selectCallback.selected("", 0);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (position != 0) {
                    File file = new File(item.url);
                    if (!file.exists()) {
                        Toast.makeText(mContext, mContext.getString(R.string.unknow_error), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "video/*");
                    mContext.startActivity(intent);
                }

                return false;
            }
        });

        RelativeLayout itemView = holder.getView(R.id.item);
        StaggeredGridLayoutManager.LayoutParams itemViewParams = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
        itemViewParams.width = mScreenWidth / 3;
        itemViewParams.height = mScreenWidth / 3;
        itemView.setLayoutParams(itemViewParams);

        if (!item.isCallStarted) {
            RequestOptions options = new RequestOptions().transform(new CenterCrop(), new RoundedCorners(DensityUtils.dp2px(4)));
            Glide.with(mContext).load(Uri.fromFile(new File(item.url))).apply(options).into((ImageView) holder.getView(R.id.imageViewFromMediaChooserGridItemRowView));
        }

        ImageView checkedView = holder.getView(R.id.selected);
        checkedView.setImageResource(item.status ? R.mipmap.ic_selected : R.mipmap.release_addpage_check_nor);

        Chronometer videoDuration = holder.getView(R.id.video_duration);
        videoDuration.setBase(SystemClock.elapsedRealtime() - item.duration);
    }

    public SelectCallback selectCallback;

    public void setSelectCallback(SelectCallback selectCallback) {
        this.selectCallback = selectCallback;
    }

    public interface SelectCallback {
        void selected(String path, long duration);
    }
}
