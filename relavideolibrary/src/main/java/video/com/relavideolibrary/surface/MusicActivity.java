package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kingsoft.media.httpcache.KSYProxyService;
import com.kingsoft.media.httpcache.OnCacheStatusListener;
import com.kingsoft.media.httpcache.OnErrorListener;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.BaseViewHolder;
import video.com.relavideolibrary.CallbackManager;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.Utils.FileManager;
import video.com.relavideolibrary.Utils.ScreenUtils;
import video.com.relavideolibrary.adapter.MusicCategoryAdapter;
import video.com.relavideolibrary.adapter.MusicListAdapter;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;
import video.com.relavideolibrary.interfaces.MusicListSyncDataCallback;
import video.com.relavideolibrary.interfaces.MusicPlayEventListener;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.MusicBean;
import video.com.relavideolibrary.model.MusicCategoryBean;
import video.com.relavideolibrary.model.MusicProgressEvent;
import video.com.relavideolibrary.view.ShaderView;
import video.com.relavideolibrary.view.VoiceControlView;

public class MusicActivity extends BaseActivity implements MusicCategoryAdapter.OnItemClickListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, MusicPlayEventListener, MusicListAdapter.OnItemClickListener, View.OnClickListener, VoiceControlView.OnVolumeListener {

    public static final String TAG = "MusicActivity";

    private TextView complete;
    private RecyclerView tabLayout;
    private RecyclerView recyclerView;
    private MusicListAdapter musicListAdapter;

    private KSYMediaPlayer ksyMediaPlayer;

    private KSYProxyService proxy;

    private boolean isAutoDismiss = true;

//    private MusicBean item;
    private float audioVolume = 1.0f;
    private float videoVolume = 1.0f;

    private MusicBean currentMusic;

    public MusicBean getCurrentMusic() {
        return currentMusic;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentBar();
        setContentView(R.layout.activity_music);
        showTranslucentView();
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.music_recycler);
        findViewById(R.id.cancel).setOnClickListener(this);
        VoiceControlView voiceControlView = findViewById(R.id.voiceControlView);
        voiceControlView.setOnVolumeListener(this);

        initTablayout();

        initMusicList();

        initProxy();

        initPlayer();

        CallbackManager.getInstance().getCallbackMap().put(MusicPlayEventListener.class.getSimpleName(), this);
    }

    private void initPlayer() {
        ksyMediaPlayer = new KSYMediaPlayer.Builder(this.getApplicationContext()).build();
        ksyMediaPlayer.setOnPreparedListener(this);
        ksyMediaPlayer.setOnInfoListener(this);
    }

    private void initProxy() {
        proxy = RelaVideoSDK.getKSYProxy();

        File file = new File(FileManager.getMusicPath());

        proxy.setCacheRoot(file);

        proxy.setMaxFilesCount(5000);

        proxy.startServer();

    }

    private void initMusicList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        musicListAdapter = new MusicListAdapter(R.layout.item_music_list, recyclerView, null);
        recyclerView.setAdapter(musicListAdapter);
        musicListAdapter.setOnItemClickListener(this);

        //默认请求一组数据
        MusicListCallback musicListCallback = (MusicListCallback) CallbackManager.getInstance().getCallbackMap().get(MusicListCallback.class.getSimpleName());
        if (musicListCallback != null) {
            musicListCallback.getMusicList(Constant.NEWEST_MUSIC_CATEGORY, new MusicListSyncDataCallback() {
                @Override
                public void onSuccess(final List<MusicBean> data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicListAdapter.setData(data);
                        }
                    });
                }

                @Override
                public void onFail() {

                }
            });
        }

    }

    private void initTablayout() {
        MusicCategoryCallback musicCategoryCallback = (MusicCategoryCallback) CallbackManager.getInstance().getCallbackMap().get(MusicCategoryCallback.class.getSimpleName());
        if (musicCategoryCallback != null) {
            List<MusicCategoryBean> musicCategoryData = musicCategoryCallback.getMusicCategoryData();

            MusicCategoryAdapter musicCategoryAdapter = new MusicCategoryAdapter(R.layout.item_category_list, tabLayout, musicCategoryData);
            tabLayout.setHasFixedSize(true);
            tabLayout.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            tabLayout.setAdapter(musicCategoryAdapter);
            musicCategoryAdapter.setOnItemClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.complete) {
            ScreenUtils.preventViewMultipleTouch(v, 2000);
            downloadMusic();
        } else if (id == R.id.cancel) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onItemClick(MusicCategoryBean item, int position) {
        MusicListCallback musicListCallback = (MusicListCallback) CallbackManager.getInstance().getCallbackMap().get(MusicListCallback.class.getSimpleName());
        if (musicListCallback != null) {
            musicListCallback.getMusicList(item.categoryCode, new MusicListSyncDataCallback() {
                @Override
                public void onSuccess(final List<MusicBean> data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicListAdapter.setData(data);
                        }
                    });
                }

                @Override
                public void onFail() {

                }
            });
        }
    }

    @Override
    public void onItemClick(BaseViewHolder holder, MusicBean item, int position) {
        if (complete == null) {
            complete = findViewById(R.id.complete);
            complete.setOnClickListener(this);
            complete.setBackground(getResources().getDrawable(R.drawable.preview_btn_bg));
            complete.setEnabled(true);
        }

        final ImageView playIcon = holder.getView(R.id.play_icon);
        final LottieAnimationView playAnim = holder.getView(R.id.play_anim);
        if (currentMusic != null && currentMusic.musicId == item.musicId) {
            if (currentMusic.isPause) {
                startMusic(item);
                playIcon.setVisibility(View.INVISIBLE);
                playAnim.setVisibility(View.VISIBLE);
                playAnim.setProgress(0);
                playAnim.playAnimation();
            } else {
                pauseMusic();
                playIcon.setVisibility(View.VISIBLE);
                playAnim.setVisibility(View.INVISIBLE);
                if (playAnim.isAnimating()) playAnim.cancelAnimation();
            }
        } else {
            startMusic(item);
            currentMusic.isLoading = true;
            musicListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ksyMediaPlayer != null && currentMusic != null && !currentMusic.isPause)
            ksyMediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.pause();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.stop();
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
        }

        if (proxy != null && musicCacheListener != null && mOnErrorListener != null && currentMusic.url != null) {
            proxy.unregisterCacheStatusListener(musicCacheListener, currentMusic.url);
            proxy.unregisterErrorListener(mOnErrorListener);
            proxy.shutDownServer();
        }

        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
        }
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        if (ksyMediaPlayer != null && isForeground) {
            // 开始播放视频
            ksyMediaPlayer.start();
            isIDEL = true;
            interval();
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            // reload成功后会有消息回调
            case IMediaPlayer.MEDIA_INFO_RELOADED:
                interval();
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START://音频开始播放
                currentMusic.isLoading = false;
                musicListAdapter.notifyDataSetChanged();
                break;
        }
        return false;
    }

    @Override
    public void onVolume(float audioVolume, float videoVolume) {
        this.audioVolume = audioVolume;
        this.videoVolume = videoVolume;
    }

    private boolean isIDEL = false;

    public void startMusic(MusicBean item) {

        if (ksyMediaPlayer == null) return;
        if (!isIDEL) {

            currentMusic = item;
            try {
                ksyMediaPlayer.setScreenOnWhilePlaying(true);
                ksyMediaPlayer.setLooping(true);
                ksyMediaPlayer.setDataSource(item.url);
                ksyMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (currentMusic.isPause) {
            if (currentMusic.musicId == item.musicId)
                restartMusic();
            else {
                switchMusic();
            }

        } else {
            switchMusic();
        }
    }

    public void downloadMusic() {

        isJumped = false;

        isAutoDismiss = true;

        File musicCacheFile = proxy.getCacheFile(currentMusic.url);

        Log.d(TAG, "getCachingPercent :" + proxy.getCachingPercent(currentMusic.url).toString());

        if (musicCacheFile != null) {
            jumpMusicEditorActivity();
            return;
        }

        showProgress();

        proxy.registerCacheStatusListener(musicCacheListener, currentMusic.url);

        proxy.registerErrorListener(mOnErrorListener);

        proxy.startPreDownload(currentMusic.url);
    }

    private OnCacheStatusListener musicCacheListener = new OnCacheStatusListener() {
        @Override
        public void OnCacheStatus(String url, long sourceLength, int percentsAvailable) {
            setDialogProgress(percentsAvailable);
            if (percentsAvailable == 100) {
                if (isAutoDismiss) {
                    jumpMusicEditorActivity();
                }
            }
        }
    };

    private OnErrorListener mOnErrorListener = new OnErrorListener() {
        @Override
        public void OnError(int i) {
            dismissDialog();
        }
    };

    boolean isJumped = false;

    private void jumpMusicEditorActivity() {

        if (!isJumped) {

            File musicCacheFile = proxy.getCacheFile(currentMusic.url);

            Log.d(TAG, " cacheFile : " + musicCacheFile);

            if (musicCacheFile == null) {
                return;
            }

            Log.d(TAG, " cacheFile.getAbsolutePath() : " + musicCacheFile.getAbsolutePath());

            String musicUrl = musicCacheFile.getAbsolutePath();

            Log.d(TAG, " musicUrl : " + musicUrl);

            dismissDialog();
            isJumped = true;

            VideoManager.getInstance().setMusicBean(currentMusic);
            VideoManager.getInstance().setMusicVolumn(audioVolume);
            VideoManager.getInstance().setVideoVolumn(videoVolume);
            setResult(Activity.RESULT_OK);
            finish();

        }
    }

    public void pauseMusic() {
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.pause();
            currentMusic.isPause = true;

        }
    }

    private void restartMusic() {
        if (ksyMediaPlayer != null && currentMusic.isPause) {
            ksyMediaPlayer.start();
            currentMusic.isPause = false;
        }
    }

    public void switchMusic() {

        ksyMediaPlayer.stop();
        ksyMediaPlayer.reset();
        ksyMediaPlayer.setScreenOnWhilePlaying(true);
        ksyMediaPlayer.setLooping(true);
        ksyMediaPlayer.setOnPreparedListener(this);
        try {
            ksyMediaPlayer.setDataSource(currentMusic.url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ksyMediaPlayer.shouldAutoPlay(false);
        ksyMediaPlayer.prepareAsync();
//        if (ksyMediaPlayer != null) {
//            ksyMediaPlayer.reload(mDataSource, true);//播放另一个
//        }
    }

    private void showProgress() {

        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, " onDismiss " + (mProgressDialog.getProgress() < 100));
                if (mProgressDialog.getProgress() < 100) {
                    isAutoDismiss = false;
                } else {
                    isAutoDismiss = true;
                }

            }
        });
        showProgressDialog();
    }

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long duration = ksyMediaPlayer.getDuration();
            long currentProgress = ksyMediaPlayer.getCurrentPosition();
            int progress = (int) (currentProgress * 1.0 / duration * 100);
//                        LogUtils.d(TAG, "druation :" + duration
//                                + "\nCurrentPosition :" + currentProgress
//                                + "\nposition :" + progress);
            MusicProgressEvent musicProgressEvent = new MusicProgressEvent();
            musicProgressEvent.setMusicPorgress(progress);
            MusicPlayEventListener musicPlayEventListener = (MusicPlayEventListener) CallbackManager.getInstance().getCallbackMap().get(MusicPlayEventListener.class.getSimpleName());
            if (musicPlayEventListener != null) {
                musicPlayEventListener.progress(musicProgressEvent);
            }
            handler.postDelayed(runnable, 100);
        }
    };

    private void interval() {
        handler.postDelayed(runnable, 100);
    }

    @Override
    public void progress(MusicProgressEvent musicProgressEvent) {

        Log.d("receiver", musicProgressEvent.toString());

        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstPosition = manager.findFirstVisibleItemPosition();
        int lastPosition = manager.findLastVisibleItemPosition();
        for (int i = 0; i < musicListAdapter.getData().size(); i++) {
            if (i >= firstPosition &&
                    i <= lastPosition &&
                    currentMusic.musicId == musicListAdapter.getData().get(i).musicId) {
                View holder = manager.findViewByPosition(i);
                if (currentMusic.progress != musicProgressEvent.getMusicPorgress()) {
                    ShaderView progress = (ShaderView) holder.findViewById(R.id.progress);
                    currentMusic.progress = musicProgressEvent.getMusicPorgress();
                    progress.setProgress(currentMusic.progress, musicListAdapter.getData().get(i).musicHours * 1000 / 100);
                }
            }
        }
    }
}
