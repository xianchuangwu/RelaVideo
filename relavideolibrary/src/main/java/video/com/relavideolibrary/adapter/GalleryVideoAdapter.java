package video.com.relavideolibrary.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Collection;

import video.com.relavideolibrary.BaseRecyclerAdapter;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.ScreenUtils;
import video.com.relavideolibrary.model.MediaModel;

/**
 * Created by chad
 * Time 17/12/8
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class GalleryVideoAdapter extends BaseRecyclerAdapter<MediaModel> {

    private int mScreenWidth;
    private final BitmapFactory.Options options;

    public GalleryVideoAdapter(@LayoutRes int layoutId, RecyclerView recyclerView, Collection<MediaModel> list) {
        super(layoutId, recyclerView, list);
        mScreenWidth = ScreenUtils.getScreenWidth(mContext);
        options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    @Override
    public void dataBinding(BaseViewHolder holder, final MediaModel item, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!item.status) {
                    File file = new File(item.url);
                    if (!file.exists()) {
                        Toast.makeText(mContext, mContext.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MediaMetadataRetriever retr = new MediaMetadataRetriever();
                    try {
                        retr.setDataSource(item.url);
                        String heightStr = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
                        String widthStr = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
                        String durationStr = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        Log.i("MediaGalleryActivity", "url :" + item.url
                                + "\nwidth :" + widthStr
                                + "\nheight :" + heightStr
                                + "\nduration :" + durationStr
                        );
                        int height = 0;
                        int width = 0;
                        long duration = 0;
                        if (!TextUtils.isEmpty(heightStr)) height = Integer.parseInt(heightStr);
                        if (!TextUtils.isEmpty(widthStr)) width = Integer.parseInt(widthStr);
                        if (!TextUtils.isEmpty(durationStr))
                            duration = Long.parseLong(durationStr);

                        int bigBorder;
                        int smallBorder;
                        if (width >= height) {
                            bigBorder = width;
                            smallBorder = height;
                        } else {
                            bigBorder = height;
                            smallBorder = width;
                        }

                        if (!(bigBorder / 16 == smallBorder / 9 && bigBorder % 16 == 0 && smallBorder % 9 == 0)) {
                            Toast.makeText(mContext, mContext.getString(R.string.video_format_not_support), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (duration > 60 * 1000) {
                            Toast.makeText(mContext, mContext.getString(R.string.video_max_duration), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (duration == 0) {
                            Toast.makeText(mContext, mContext.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, mContext.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                        return;
                    } finally {
                        retr.release();
                    }
                }

                if (!item.status) {//未选中
                    for (int i = 0; i < mData.size(); i++) {
                        if (i != position)
                            mData.get(i).status = false;
                        else mData.get(i).status = true;
                    }
                    notifyDataSetChanged();
                    if (selectCallback != null) selectCallback.selected(item.url);
                } else {
                    item.status = !item.status;
                    notifyItemChanged(position);
                    if (selectCallback != null) selectCallback.selected("");
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                File file = new File(item.url);
                if (!file.exists()) {
                    Toast.makeText(mContext, mContext.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                mContext.startActivity(intent);
                return false;
            }
        });

        RelativeLayout itemView = holder.getView(R.id.item);
        StaggeredGridLayoutManager.LayoutParams itemViewParams = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
        itemViewParams.width = mScreenWidth / 3;
        itemViewParams.height = mScreenWidth / 3;
        itemView.setLayoutParams(itemViewParams);

        if (!item.isCallStarted) {

            Glide.with(mContext).load(item.url).asBitmap().into((ImageView) holder.getView(R.id.imageViewFromMediaChooserGridItemRowView));
        }

        ImageView checkedView = holder.getView(R.id.selected);
        if (item.status) {
            checkedView.setVisibility(View.VISIBLE);
        } else {
            checkedView.setVisibility(View.INVISIBLE);
        }

        Chronometer videoDuration = holder.getView(R.id.video_duration);
        videoDuration.setBase(SystemClock.elapsedRealtime() - item.duration);
    }

    public SelectCallback selectCallback;

    public void setSelectCallback(SelectCallback selectCallback) {
        this.selectCallback = selectCallback;
    }

    public interface SelectCallback {
        void selected(String path);
    }
}
