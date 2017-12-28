package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RawRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import baidu.filter.GPUImageExtRotationTexFilter;
import baidu.measure.IRenderView;
import baidu.measure.TextureRenderView;
import google.grafika.gles.EglCore;
import google.grafika.gles.GlUtil;
import google.grafika.gles.WindowSurface;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.CallbackManager;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.adapter.FilterAdapter;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.thread.EditVideoThread;

public class EditActivity extends BaseActivity implements TextureView.SurfaceTextureListener
        , FilterAdapter.OnItemClickListener, View.OnClickListener, EditVideoThread.EditVideoListener {

    public static final String TAG = "EditActivity";

    private TextureRenderView textureView;

    private RenderThread mRenderThread;

    private ImageView filter_image;

    private ImageView play;

    private TextView music_name;

    private RecyclerView filter_recyclerView;

    private int currentFilterId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0修改状态栏字体色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.0 以上直接设置状态栏颜色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white_10_alpha));
            }
        }
        setContentView(R.layout.activity_edit);
        showTranslucentView();
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.cut_img).setOnClickListener(this);
        findViewById(R.id.music_img).setOnClickListener(this);
        music_name = findViewById(R.id.music_name);
        filter_recyclerView = findViewById(R.id.filter_recycler);
        filter_image = findViewById(R.id.filter_image);
        play = findViewById(R.id.play);
        filter_image.setOnClickListener(this);

        initVideoView();
        initFilterList();
        createRenderThread();
    }

    private void initFilterList() {

        FilterDataCallback filterDataCallback = (FilterDataCallback) CallbackManager.getInstance().getCallbackMap().get(FilterDataCallback.class.getSimpleName());
        if (filterDataCallback != null) {
            ArrayList<FilterBean> list = (ArrayList<FilterBean>) filterDataCallback.getFilterData();
            FilterAdapter filterAdapter = new FilterAdapter(R.layout.item_filter_list
                    , filter_recyclerView
                    , list);
            filter_recyclerView.setHasFixedSize(true);
            filter_recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            filter_recyclerView.setAdapter(filterAdapter);
            filterAdapter.setOnItemClickListener(this);
        }
    }

    private void initVideoView() {
        RelativeLayout video_container = findViewById(R.id.video_container);
        textureView = new TextureRenderView(this);
        textureView.setId(R.id.edit_video_id);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        video_container.addView(textureView, layoutParams);
        textureView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        textureView.setSurfaceTextureListener(this);
        textureView.setOnClickListener(this);
    }

    private void createRenderThread() {
        mRenderThread = new RenderThread(this);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();
    }

    private void destroyRenderThread() {
        if (mRenderThread == null) return;
        RenderHandler rh = mRenderThread.getHandler();
        rh.sendMediaPlayerRelease();
        rh.sendShutdown();
        try {
            mRenderThread.join();
        } catch (InterruptedException ie) {
            // not expected
            throw new RuntimeException("join was interrupted", ie);
        }
        mRenderThread = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRenderThread != null) {
            RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
            mRenderThreadHandler.sendResume();
            play.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRenderThread != null) {
            RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
            mRenderThreadHandler.sendPause();
            play.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyRenderThread();
        Log.i(TAG, "onPause END");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IntentCode.REQUEST_CODE_MUSIC && resultCode == Activity.RESULT_OK) {
            music_name.setText(VideoManager.getInstance().getMusicBean().name);
            music_name.setVisibility(View.VISIBLE);
            if (filter_recyclerView.getVisibility() != View.GONE) {
                filter_recyclerView.setVisibility(View.GONE);
                filter_image.setImageResource(R.mipmap.ic_filter);
            }

            if (mRenderThread != null) {
                RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
                mRenderThreadHandler.sendRestartVideo();
            }
        } else if (requestCode == Constant.IntentCode.REQUEST_CODE_CUT && resultCode == Activity.RESULT_OK) {
            if (mRenderThread != null) {
                RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
                mRenderThreadHandler.sendReloadVideo();
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureAvailable surfaceTexture=" + surfaceTexture.hashCode() + ";width=" + i + ";height=" + i1);

        if (mRenderThread != null) {
            // Normal case -- render thread is running, tell it about the new surface.
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceAvailable(surfaceTexture, i, i1);
        } else {
            Log.i(TAG, "render thread not running");
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d(TAG, "onSurfaceTextureSizeChanged surfaceTexture=" + surfaceTexture.hashCode() + ";width=" + i + ";height=" + i1);
        if (mRenderThread != null) {
            // Normal case -- render thread is running, tell it about the new surface.
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceChanged(surfaceTexture, i, i1);
        } else {
            Log.i(TAG, "render thread not running");
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroyed surfaceTexture=" + surfaceTexture.hashCode());
        if (mRenderThread != null) {
            RenderHandler rh = mRenderThread.getHandler();
            rh.sendSurfaceDestroyed(surfaceTexture);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//        Log.d(TAG, "onSurfaceTextureUpdated surfaceTexture=" + surfaceTexture.hashCode());
    }

    @Override
    public void itemClick(int filterId) {
        //切换滤镜
        RenderHandler renderHandler = mRenderThread.getHandler();
        renderHandler.sendSwitchFilter(filterId);
        currentFilterId = filterId;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.filter_image) {
            if (filter_recyclerView.getVisibility() == View.VISIBLE) {
                filter_image.setImageResource(R.mipmap.ic_filter);
                filter_recyclerView.setVisibility(View.GONE);
                String name = VideoManager.getInstance().getMusicBean().name;
                if (!TextUtils.isEmpty(name)) {
                    music_name.setVisibility(View.VISIBLE);
                    music_name.setText(name);
                }
            } else {
                filter_image.setImageResource(R.mipmap.ic_filter_seleted);
                filter_recyclerView.setVisibility(View.VISIBLE);
                music_name.setVisibility(View.GONE);
            }
        } else if (id == R.id.music_img) {
            startActivityForResult(new Intent(this, MusicActivity.class), Constant.IntentCode.REQUEST_CODE_MUSIC);
        } else if (id == R.id.cut_img) {
            startActivityForResult(new Intent(this, CutActivity.class), Constant.IntentCode.REQUEST_CODE_CUT);
        } else if (id == R.id.edit_video_id) {
            if (play.getVisibility() == View.INVISIBLE) {
                play.setVisibility(View.VISIBLE);
                if (mRenderThread != null) {
                    RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
                    mRenderThreadHandler.sendPause();
                }
            } else {
                play.setVisibility(View.INVISIBLE);
                if (mRenderThread != null) {
                    RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
                    mRenderThreadHandler.sendResume();
                }
            }
        } else if (id == R.id.next) {
            VideoManager.getInstance().getVideoBean().filterId = currentFilterId;
            showDialog();
            new EditVideoThread(this).start();
        }
    }

    @Override
    public void onEditVideoSuccess(String path) {

        dismissDialog();

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString(Constant.BundleConstants.FINAL_VIDEO_PATH, path);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onEditVideoError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
                Toast.makeText(EditActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_RELEASE_PLAYER = 8;
        private static final int MSG_REDRAW = 9;
        private static final int MSG_SWITCH_FILTER = 10;
        private static final int MSG_RESEUME_START_PLAY = 12;
        private static final int MSG_PAUSE_STOP_PLAY = 13;
        private static final int MSG_RELOAD_VIDEO = 14;
        private static final int MSG_RESTART_VIDEO = 15;

        // This shouldn't need to be a weak ref, since we'll go away when the Looper quits,
        // but no real harm in it.
        private WeakReference<RenderThread> mWeakRenderThread;

        /**
         * Call from render thread.
         */
        public RenderHandler(RenderThread rt) {
            mWeakRenderThread = new WeakReference<RenderThread>(rt);
        }

        /**
         * Sends the "surface available" message.  If the surface was newly created (i.e.
         * this is called from surfaceCreated()), set newSurface to true.  If this is
         * being called during Activity startup for a previously-existing surface, set
         * newSurface to false.
         * <p>
         * The flag tells the caller whether or not it can expect a surfaceChanged() to
         * arrive very soon.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceAvailable(SurfaceTexture holder, int width, int height) {
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE,
                    width, height, holder));
        }

        /**
         * Sends the "surface changed" message, forwarding what we got from the SurfaceHolder.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceChanged(SurfaceTexture holder, int width,
                                       int height) {
            // ignore format
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height, holder));
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendSurfaceDestroyed(SurfaceTexture holder) {
            sendMessage(obtainMessage(MSG_SURFACE_DESTROYED, 0, 0, holder));
        }

        public void sendMediaPlayerRelease() {
            sendEmptyMessage(MSG_RELEASE_PLAYER);
        }

        /**
         * Sends the "shutdown" message, which tells the render thread to halt.
         * <p>
         * Call from UI thread.
         */
        public void sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN));
        }

        public void sendResume() {
            sendEmptyMessage(MSG_RESEUME_START_PLAY);

        }

        public void sendPause() {
            sendEmptyMessage(MSG_PAUSE_STOP_PLAY);
        }

        /**
         * Sends the "frame available" message.
         * <p>
         * Call from UI thread.
         */
        public void sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE));
        }

        public void sendSwitchFilter(@RawRes int fiterId) {
            sendMessage(obtainMessage(MSG_SWITCH_FILTER, fiterId));
        }

        public void sendReloadVideo() {
            sendEmptyMessage(MSG_RELOAD_VIDEO);
        }

        public void sendRestartVideo() {
            sendEmptyMessage(MSG_RESTART_VIDEO);
        }

        @Override  // runs on RenderThread
        public void handleMessage(Message msg) {
            int what = msg.what;
            //Log.i(TAG, "RenderHandler [" + this + "]: what=" + what);

            RenderThread renderThread = mWeakRenderThread.get();
            if (renderThread == null) {
                Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                return;
            }

            switch (what) {
                case MSG_SURFACE_AVAILABLE:
                    renderThread.surfaceAvailable((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_CHANGED:
                    renderThread.surfaceChanged((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_DESTROYED:
                    renderThread.surfaceDestroyed((SurfaceTexture) msg.obj);
                    break;
                case MSG_SHUTDOWN:
                    renderThread.shutdown();
                    break;
                case MSG_FRAME_AVAILABLE:
                    renderThread.frameAvailable();
                    break;

                case MSG_REDRAW:
                    renderThread.draw();
                    break;
                case MSG_RELEASE_PLAYER:
                    renderThread.releaseMediaPlayer();
                    break;
                case MSG_SWITCH_FILTER:
                    int filterId = (int) msg.obj;
                    renderThread.switchFilter(filterId);
                    break;
                case MSG_RESEUME_START_PLAY:
                    renderThread.onResume();
                    break;
                case MSG_PAUSE_STOP_PLAY:
                    renderThread.onPause();
                    break;
                case MSG_RELOAD_VIDEO:
                    renderThread.reloadVideo();
                    break;
                case MSG_RESTART_VIDEO:
                    renderThread.restartVideo();
                    break;
                default:
                    throw new RuntimeException("unknown message " + what);
            }
        }
    }

    /**
     * Thread that handles all rendering and camera operations.
     */
    private class RenderThread extends Thread implements
            SurfaceTexture.OnFrameAvailableListener {
        // Object must be created on render thread to get correct Looper, but is used from
        // UI thread, so we need to declare it volatile to ensure the UI thread sees a fully
        // constructed object.
        private volatile RenderHandler mHandler;

        // Used to wait for the thread to start.
        private Object mStartLock = new Object();
        private boolean mReady = false;

        private EglCore mEglCore;

        // Receives the output from the camera preview.
        private SurfaceTexture mCameraTexture;

        private Activity activity;

        /**
         * Constructor.  Pass in the MainHandler, which allows us to send stuff back to the
         * Activity.
         */
        public RenderThread(Activity activity) {
            this.activity = activity;
        }

        /**
         * Thread entry point.
         */
        @Override
        public void run() {
            Looper.prepare();

            // We need to create the Handler before reporting ready.
            mHandler = new RenderHandler(this);
            synchronized (mStartLock) {
                mReady = true;
                mStartLock.notify();    // signal waitUntilReady()
            }

            // Prepare EGL and open the camera before we start handling messages.
            mEglCore = new EglCore(null, 0);
            initPlayer();

            Looper.loop();

            Log.i(TAG, "looper quit");

            if (gpuImageFilter != null) {
                gpuImageFilter.destroy();
                gpuImageFilter = null;
            }

            mEglCore.release();

            synchronized (mStartLock) {
                mReady = false;
            }
        }

        private MediaPlayer videoPlayer;

        private void initPlayer() {
            if (TextUtils.isEmpty(VideoManager.getInstance().getVideoBean().videoPath)) return;
            videoPlayer = new MediaPlayer();
            try {
                videoPlayer.setDataSource(VideoManager.getInstance().getVideoBean().videoPath);
                videoPlayer.prepareAsync();
                videoPlayer.setLooping(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer iMediaPlayer) {
                    if (textureView != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                            }
                        });
                    }

                    VideoManager.getInstance().getMusicBean().startTime = 0;
                    VideoManager.getInstance().getMusicBean().endTime = videoPlayer.getDuration();

                    float videoVolumn = VideoManager.getInstance().getVideoVolumn();
                    videoPlayer.setVolume(videoVolumn, videoVolumn);
                    videoPlayer.start();

                    long musicSeek = 0;
                    float musicVolumn = VideoManager.getInstance().getMusicVolumn();
                    String musicPath = VideoManager.getInstance().getMusicBean().url;
                    try {
                        RenderThread.this.musicSeek = musicSeek;
                        RenderThread.this.musicVolumn = musicVolumn;
                        playBGM(musicPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            videoPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(final MediaPlayer iMediaPlayer, int width, int height) {
                    if (textureView != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textureView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                            }
                        });
                    }
                }
            });

            videoPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                        if (textureView != null)
                            textureView.setVideoRotation(extra);
                    }
                    return true;
                }
            });
        }

        private void reloadVideo() {
            if (videoPlayer != null) {
                videoPlayer.release();
                videoPlayer = null;
            }
            if (musicPlayer != null) musicPlayer.pause();
            initPlayer();
        }

        private void restartVideo() {
            if (videoPlayer != null) {
                videoPlayer.stop();
                videoPlayer.prepareAsync();
            }
        }

        private KSYMediaPlayer musicPlayer;
        long musicSeek = 0;
        float musicVolumn;

        private void playBGM(String url) throws IOException {

            if (TextUtils.isEmpty(url)) {
                if (musicPlayer != null && musicPlayer.isPlaying())
                    musicPlayer.stop();
                return;
            }
            if (musicPlayer == null) {
                musicPlayer = new KSYMediaPlayer.Builder(activity.getApplicationContext()).build();
                musicPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer mp) {
                        musicPlayer.setVolume(musicVolumn, musicVolumn);
                        musicPlayer.seekTo(musicSeek);
                    }
                });
                musicPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(IMediaPlayer mp) {
                        musicPlayer.start();
                    }
                });
                musicPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                        if (what == com.ksyun.media.player.IMediaPlayer.MEDIA_INFO_RELOADED) {
                            musicPlayer.setVolume(musicVolumn, musicVolumn);
                            musicPlayer.seekTo(musicSeek);
                        }
                        return false;
                    }
                });
                musicPlayer.setLooping(true);
                musicPlayer.setDataSource(url);
                musicPlayer.prepareAsync();
            } else {
                musicPlayer.reload(url, false);
            }

        }

        private void onResume() {
            if (videoPlayer != null && !videoPlayer.isPlaying())
                videoPlayer.start();
            if (musicPlayer != null && !musicPlayer.isPlaying())
                musicPlayer.start();
        }

        private void onPause() {
            if (videoPlayer != null && videoPlayer.isPlaying())
                videoPlayer.pause();
            if (musicPlayer != null && musicPlayer.isPlaying())
                musicPlayer.pause();
        }

        private void releaseMediaPlayer() {
            if (videoPlayer != null) videoPlayer.release();
            if (musicPlayer != null) musicPlayer.release();
        }

        /**
         * Waits until the render thread is ready to receive messages.
         * <p>
         * Call from the UI thread.
         */
        public void waitUntilReady() {
            synchronized (mStartLock) {
                while (!mReady) {
                    try {
                        mStartLock.wait();
                    } catch (InterruptedException ie) { /* not expected */ }
                }
            }
        }

        /**
         * Shuts everything down.
         */
        private void shutdown() {
            Log.i(TAG, "shutdown");
            Looper.myLooper().quit();
        }

        /**
         * Returns the render thread's Handler.  This may be called from any thread.
         */
        public RenderHandler getHandler() {
            return mHandler;
        }

        int mTextureId = -1;

        final float CUBE[] = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f,
        };
        private FloatBuffer mGLCubeBuffer;
        private FloatBuffer mGLTextureBuffer;

        HashMap<SurfaceTexture, WindowSurface> windowSurfacesMap = new HashMap<SurfaceTexture, WindowSurface>();

        WindowSurface mWindowSurface1;
        GPUImageFilterGroup gpuImageFilter;

        int surfaceWidth = 0;
        int surfaceHeight = 0;

        /**
         * Handles the surface-created callback from SurfaceView.  Prepares GLES and the Surface.
         */
        private void surfaceAvailable(SurfaceTexture holder, int width, int height) {

            Log.i(TAG, "RenderThread surfaceCreated holder=" + holder.hashCode());
            Surface surface = new Surface(holder);
            mWindowSurface1 = new WindowSurface(mEglCore, surface, false);
            synchronized (windowSurfacesMap) {
                windowSurfacesMap.put(holder, mWindowSurface1);
                mWindowSurface1.makeCurrent();
            }
            GLES20.glViewport(0, 0, width, height);

            if (windowSurfacesMap.size() <= 1) {
                // only create once

                mTextureId = getPreviewTexture();
                Log.i(TAG, "mTextureId=" + mTextureId);
                mCameraTexture = new SurfaceTexture(mTextureId);

//                mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
//                        .order(ByteOrder.nativeOrder())
//                        .asFloatBuffer();
//                mGLCubeBuffer.put(CUBE).position(0);
//
//                mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
//                        .order(ByteOrder.nativeOrder())
//                        .asFloatBuffer();
//                mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0);

                mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mGLCubeBuffer.put(CUBE).position(0);

                mGLTextureBuffer = ByteBuffer.allocateDirect(GPUImageExtRotationTexFilter.FULL_RECTANGLE_TEX_COORDS.length * 4)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
                mGLTextureBuffer.put(GPUImageExtRotationTexFilter.FULL_RECTANGLE_TEX_COORDS).position(0);


                Log.i(TAG, "surfaceChanged should only once here");
                gpuImageFilter = generateGPUImageFilter(-1);
                gpuImageFilter.init();

                surfaceWidth = width;
                surfaceHeight = height;
                GLES20.glUseProgram(gpuImageFilter.getProgram());
                gpuImageFilter.onOutputSizeChanged(width, height);

                mCameraTexture.setOnFrameAvailableListener(this);
                finishSurfaceSetup();
            }

        }

        /**
         * Handles the surfaceChanged message.
         * <p>
         * We always receive surfaceChanged() after surfaceCreated(), but surfaceAvailable()
         * could also be called with a Surface created on a previous run.  So this may not
         * be called.
         */
        private void surfaceChanged(SurfaceTexture surfaceHolder, int width, int height) {
            Log.i(TAG, "RenderThread surfaceChanged " + width + "x" + height + ";surfaceHolder=" + surfaceHolder.hashCode());

        }

        /**
         * Handles the surfaceDestroyed message.
         */
        private void surfaceDestroyed(SurfaceTexture surfaceHolder) {
            // In practice this never appears to be called -- the activity is always paused
            // before the surface is destroyed.  In theory it could be called though.
//            Log.i(TAG, "RenderThread surfaceDestroyed holder=" + surfaceHolder.hashCode());
            releaseGl(surfaceHolder);
        }

        public int getPreviewTexture() {
            int textureId = -1;
            if (textureId == GlUtil.NO_TEXTURE) {
                textureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
            }
            return textureId;
        }

        /**
         * Releases most of the GL resources we currently hold (anything allocated by
         * surfaceAvailable()).
         * <p>
         * Does not release EglCore.
         */
        private void releaseGl(SurfaceTexture surfaceHolder) {
            GlUtil.checkGlError("releaseGl start");

            WindowSurface windowSurface = windowSurfacesMap.get(surfaceHolder);
            if (windowSurface != null) {

                windowSurfacesMap.remove(surfaceHolder);
                windowSurface.release();
            }

            GlUtil.checkGlError("releaseGl done");

        }

        /**
         * Sets up anything that depends on the window size.
         * <p>
         * Open the camera (to set mCameraAspectRatio) before calling here.
         */
        private void finishSurfaceSetup() {
            videoPlayer.setSurface(new Surface(mCameraTexture));
        }

        @Override   // SurfaceTexture.OnFrameAvailableListener; runs on arbitrary thread
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            mHandler.sendFrameAvailable();
        }

        /**
         * Handles incoming frame of data from the camera.
         */
        private void frameAvailable() {
            mCameraTexture.updateTexImage();
            mCameraTexture.getTransformMatrix(mSTMatrix);
            draw();
        }

        /**
         * Draws the scene and submits the buffer.
         */
        private void draw() {

            if (gpuImageFilter != null) {
                GlUtil.checkGlError("draw start >");
                WindowSurface windowSurface = mWindowSurface1;
                windowSurface.makeCurrent();
                gpuImageFilter.onDraw(mTextureId, mGLCubeBuffer, mGLTextureBuffer);
                windowSurface.swapBuffers();
                GlUtil.checkGlError("draw done >");
            }
        }

        private void switchFilter(@RawRes int filterId) {
            if (gpuImageFilter != null) {
                gpuImageFilter.destroy();
                gpuImageFilter = null;
            }

            gpuImageFilter = generateGPUImageFilter(filterId);
            gpuImageFilter.init();
            GLES20.glUseProgram(gpuImageFilter.getProgram());
            gpuImageFilter.onOutputSizeChanged(surfaceWidth, surfaceHeight);
        }

    }

    float[] mSTMatrix = new float[16];

    private GPUImageFilterGroup generateGPUImageFilter(@RawRes int filterId) {
        GPUImageFilterGroup gpuImageFilter = new GPUImageFilterGroup();
//        gpuImageFilter.addFilter(new GPUImageExtTexFilter());
        GPUImageExtRotationTexFilter ext = new GPUImageExtRotationTexFilter();
        ext.setTexMatrix(mSTMatrix);
        gpuImageFilter.addFilter(ext);
        if (filterId == -1) {
            //如果没有为当前视频设置过滤镜参数,就为它设置个原始滤镜
            gpuImageFilter.addFilter(new GPUImageFilter());
        } else {

            GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
            lookupFilter.setBitmap(BitmapFactory.decodeResource(getResources(), filterId));
            gpuImageFilter.addFilter(lookupFilter);
        }
        return gpuImageFilter;
    }
}
