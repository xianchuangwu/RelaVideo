package video.com.relavideodemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.io.IOException;
import java.util.List;

/**
 * Created by chad
 * Time 18/2/3
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class VideoListView extends RecyclerViewPager implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener
        , TextureView.SurfaceTextureListener {

    public static final String TAG = "VideoListView";

    private KSYMediaPlayer ksyMediaPlayer;
    private Context context;

    public VideoListView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VideoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public VideoListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        ksyMediaPlayer = new KSYMediaPlayer.Builder(context.getApplicationContext()).build();
        ksyMediaPlayer.setOnPreparedListener(this);
        ksyMediaPlayer.setOnInfoListener(this);

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        setLayoutManager(layout);
        addOnPageChangedListener(onPageChangedListener);
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled");
            }
        });
    }

    public void setData(List<String> list) {
        MyAdapter myAdapter = new MyAdapter(list);
        setAdapter(myAdapter);
        scrollToPosition(0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnPageChangedListener(onPageChangedListener);
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
        }
    }


    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (ksyMediaPlayer != null) ksyMediaPlayer.start();
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        switch (i) {
            case KSYMediaPlayer.MEDIA_INFO_BUFFERING_START:
                break;
            case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                MyAdapter.ViewHolder viewHolder = (MyAdapter.ViewHolder) findViewHolderForLayoutPosition(getCurrentPosition());
                viewHolder.thumb.setVisibility(View.INVISIBLE);
                break;
        }
        return false;
    }

    private RecyclerViewPager.OnPageChangedListener onPageChangedListener = new OnPageChangedListener() {
        @Override
        public void OnPageChanged(int beforePos, int currentPos) {
            Log.d("OnPageChanged", "beforePosition: " + beforePos + "\ncurrentPosition: " + currentPos);

            //RecyclerviewPager刚初始化时,由于调用了scrollToPosition(0),此时beforePosition＝currentPosition＝0;
            if (beforePos != currentPos) {
                getAdapter().notifyItemChanged(beforePos);
            }

            MyAdapter.ViewHolder viewHolder = (MyAdapter.ViewHolder) findViewHolderForLayoutPosition(currentPos);
            viewHolder.thumb.setVisibility(View.VISIBLE);

            TextureView textureView = new TextureView(context);
            textureView.setSurfaceTextureListener(VideoListView.this);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.textureContainer.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            viewHolder.textureContainer.addView(textureView, layoutParams);

            try {
                if (ksyMediaPlayer != null) {
                    ksyMediaPlayer.reset();
                    ksyMediaPlayer.setDataSource(((MyAdapter) getAdapter()).getData().get(currentPos));
                    ksyMediaPlayer.setLooping(true);
                    ksyMediaPlayer.prepareAsync();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureAvailable: " + getCurrentPosition());
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.setSurface(new Surface(surface)); // 非常重要，reset之后需重新设置Surface，播放器才能渲染
            ksyMediaPlayer.setScreenOnWhilePlaying(true);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureSizeChanged: " + getCurrentPosition());

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureDestroyed: " + getCurrentPosition());
        // 此处非常重要，必须调用!!!
//        if (ksyMediaPlayer != null) {
//            ksyMediaPlayer.setSurface(null);
//        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<String> mData;
        private Context mContext;

        public MyAdapter(List<String> data) {
            mData = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            mContext = parent.getContext();
            return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_video_list, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText("position: " + position + "\n" + mData.get(position));
            holder.thumb.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public List<String> getData() {
            return mData;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public RelativeLayout textureContainer;
            public ImageView thumb;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
                textureContainer = itemView.findViewById(R.id.textureContainer);
                thumb = itemView.findViewById(R.id.thumb);
            }
        }
    }
}
