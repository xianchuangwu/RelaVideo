package video.com.relavideodemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.support.annotation.Nullable;
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

import baidu.measure.IRenderView;
import baidu.measure.TextureRenderView;

/**
 * Created by chad
 * Time 18/2/3
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class VideoListView2 extends RelativeLayout implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, TextureView.SurfaceTextureListener, IMediaPlayer.OnVideoSizeChangedListener {

    private Context context;

    private KSYMediaPlayer ksyMediaPlayer;
    private RecyclerViewPager recyclerViewPager;
    private MyAdapter myAdapter;
    private TextureRenderView textureView;

    public VideoListView2(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VideoListView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public VideoListView2(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public void setData(List<String> list) {
        myAdapter = new MyAdapter(list);
        recyclerViewPager.setAdapter(myAdapter);

        if (ksyMediaPlayer != null) {
            try {
                ksyMediaPlayer.setDataSource(myAdapter.getData().get(0));
                ksyMediaPlayer.setLooping(true);
                ksyMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {

        ksyMediaPlayer = new KSYMediaPlayer.Builder(context.getApplicationContext()).build();
        ksyMediaPlayer.setOnPreparedListener(this);
        ksyMediaPlayer.setOnInfoListener(this);
        ksyMediaPlayer.setOnVideoSizeChangedListener(this);

        View view = inflate(context, R.layout.video_list_view, this);

        textureView = view.findViewById(R.id.textureView);
        textureView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        textureView.setSurfaceTextureListener(this);

        recyclerViewPager = view.findViewById(R.id.list);
        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerViewPager.setLayoutManager(layout);
        recyclerViewPager.addOnPageChangedListener(onPageChangedListener);
        recyclerViewPager.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                textureView.scrollBy(dx, dy);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclerViewPager.removeOnPageChangedListener(onPageChangedListener);
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(final IMediaPlayer iMediaPlayer) {
        if (textureView != null) {
            textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
        }
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.start();
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
        if (textureView != null) {
            textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        if (i == KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            MyAdapter.ViewHolder viewHolder = (MyAdapter.ViewHolder) recyclerViewPager.findViewHolderForLayoutPosition(recyclerViewPager.getCurrentPosition());
            viewHolder.thumb.setVisibility(INVISIBLE);
        }
        return false;
    }

    private RecyclerViewPager.OnPageChangedListener onPageChangedListener = new RecyclerViewPager.OnPageChangedListener() {
        @Override
        public void OnPageChanged(int beforePos, int currentPos) {
            Log.d("OnPageChanged", "beforePosition: " + beforePos + "\ncurrentPosition: " + currentPos);
            //RecyclerviewPager刚初始化时,由于调用了scrollToPosition(0),此时beforePosition＝currentPosition＝0;
            if (beforePos != currentPos) {
                recyclerViewPager.getAdapter().notifyItemChanged(beforePos);
            }
            textureView.scrollTo(0, 0);
            if (ksyMediaPlayer != null)
                ksyMediaPlayer.reload(myAdapter.getData().get(currentPos), true);
        }
    };

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureAvailable: ");
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.setSurface(new Surface(surface)); // 非常重要，reset之后需重新设置Surface，播放器才能渲染
            ksyMediaPlayer.setScreenOnWhilePlaying(true);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureSizeChanged: ");

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d("SurfaceTextureListener", "onSurfaceTextureDestroyed: ");
        // 此处非常重要，必须调用!!!
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.setSurface(null);
        }
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
