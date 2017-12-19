package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RawRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksyun.media.player.IMediaPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.adapter.FilterAdapter;
import video.com.relavideolibrary.baidu.IRenderView;
import video.com.relavideolibrary.baidu.TextureRenderView;
import video.com.relavideolibrary.baidu.egl.EglCore;
import video.com.relavideolibrary.baidu.egl.GlUtil;
import video.com.relavideolibrary.baidu.egl.WindowSurface;
import video.com.relavideolibrary.filter.GPUImageExtRotationTexFilter;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.FilterBean;

public class EditActivity extends BaseActivity implements TextureView.SurfaceTextureListener
        , FilterAdapter.OnItemClickListener, View.OnClickListener {

    public static final String TAG = "EditActivity";

    private TextureRenderView textureView;

    private RenderThread mRenderThread;

    private ImageView filter_image;
    private RecyclerView filter_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        findViewById(R.id.cancel).setOnClickListener(this);
        filter_recyclerView = findViewById(R.id.filter_recycler);
        filter_image = findViewById(R.id.filter_image);
        filter_image.setOnClickListener(this);
        filter_image.setTag(true);

        initVideoView();
        initFilterList();
    }


    private void initFilterList() {

        FilterDataCallback callback = RelaVideoSDK.getCallback();
        if (callback != null) {
            ArrayList<FilterBean> list = (ArrayList<FilterBean>) callback.onComplete();
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        video_container.addView(textureView, layoutParams);
        textureView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume BEGIN");
        super.onResume();

        mRenderThread = new RenderThread(this);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();

        Log.i(TAG, "onResume END");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause BEGIN");
        super.onPause();

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
        Log.i(TAG, "onPause END");
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.filter_image) {
            boolean isShow = (boolean) v.getTag();
            if (isShow) {
                filter_image.setImageResource(R.mipmap.ic_filter);
                filter_recyclerView.setVisibility(View.GONE);
            } else {
                filter_image.setImageResource(R.mipmap.ic_filter_seleted);
                filter_recyclerView.setVisibility(View.VISIBLE);
            }
            v.setTag(!isShow);
        }
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

        private MediaPlayer mediaPlayer;

        private void initPlayer() {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(VideoManager.getInstance().getVideoBean().videoPath);
                mediaPlayer.prepareAsync();
                mediaPlayer.setLooping(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
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
                    mediaPlayer.start();
                }
            });

            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
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

            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int i, int i1) {
                    if (i == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                        if (textureView != null)
                            textureView.setVideoRotation(i1);
                    }

                    return true;
                }
            });
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


        private void releaseMediaPlayer() {
            mediaPlayer.release();
        }

        /**
         * Sets up anything that depends on the window size.
         * <p>
         * Open the camera (to set mCameraAspectRatio) before calling here.
         */
        private void finishSurfaceSetup() {
            mediaPlayer.setSurface(new Surface(mCameraTexture));
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
