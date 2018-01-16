package video.com.relavideolibrary.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import baidu.filter.GPUImageExtRotationTexFilter;
import baidu.measure.IRenderView;
import baidu.measure.TextureRenderView;
import google.grafika.gles.EglCore;
import google.grafika.gles.GlUtil;
import google.grafika.gles.WindowSurface;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;

/**
 * Created by chad
 * Time 18/1/13
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 直播大礼物自定义view 1.使用此view必须开启硬件加速 2.使用此view必须动态添加，需要一个默认播放路径
 */

public class RelaBigGiftView extends TextureRenderView implements TextureView.SurfaceTextureListener {

    private static final String TAG = "RelaBigGiftView";

    private RenderThread mRenderThread;

    private String defaultUrl;

    private Context context;

    public RelaBigGiftView(Context context, @Nullable String defaultUrl) {
        super(context);
        this.defaultUrl = defaultUrl;
        this.context = context;
    }

    /**
     * 切换播放路径
     *
     * @param url
     */
    public void reload(String url) {
        if (mRenderThread != null) {
            RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
            mRenderThreadHandler.sendReloadVideo(url);
        }
    }

    /**
     * 继续播放
     */
    public void onResume() {
        if (mRenderThread != null) {
            RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
            mRenderThreadHandler.sendResume();
        }
    }

    /**
     * 暂停播放
     */
    public void onPause() {
        if (mRenderThread != null) {
            RenderHandler mRenderThreadHandler = mRenderThread.getHandler();
            mRenderThreadHandler.sendPause();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setOpaque(false);
        this.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        this.setSurfaceTextureListener(this);
        Log.d(TAG, "setSurfaceTextureListener");

        mRenderThread = new RenderThread((Activity) context);
        mRenderThread.setName("TexFromCam Render");
        mRenderThread.start();
        mRenderThread.waitUntilReady();
        Log.d(TAG, "RenderThread start");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
        Log.d(TAG, "RenderThread destroy");

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
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//        Log.d(TAG, "onSurfaceTextureUpdated surfaceTexture=" + surfaceTexture.hashCode());
    }

    private static class RenderHandler extends Handler {
        private static final int MSG_SURFACE_AVAILABLE = 0;
        private static final int MSG_SURFACE_CHANGED = 1;
        private static final int MSG_SURFACE_DESTROYED = 2;
        private static final int MSG_SHUTDOWN = 3;
        private static final int MSG_FRAME_AVAILABLE = 4;
        private static final int MSG_RELEASE_PLAYER = 8;
        private static final int MSG_REDRAW = 9;
        private static final int MSG_RESEUME_START_PLAY = 12;
        private static final int MSG_PAUSE_STOP_PLAY = 13;
        private static final int MSG_RELOAD_VIDEO = 14;

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

        public void sendReloadVideo(String url) {
            sendMessage(obtainMessage(MSG_RELOAD_VIDEO, url));
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
                case MSG_RESEUME_START_PLAY:
                    renderThread.onResume();
                    break;
                case MSG_PAUSE_STOP_PLAY:
                    renderThread.onPause();
                    break;
                case MSG_RELOAD_VIDEO:
                    String url = (String) msg.obj;
                    renderThread.reloadVideo(url);
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

        private KSYMediaPlayer videoPlayer;

        private void initPlayer() {
            videoPlayer = new KSYMediaPlayer.Builder(activity.getApplicationContext()).build();
            videoPlayer.setLooping(false);
            try {
                videoPlayer.setDataSource(defaultUrl);
                videoPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }

            videoPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final IMediaPlayer iMediaPlayer) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RelaBigGiftView.this.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                        }
                    });
                    videoPlayer.start();
                }
            });

            videoPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(final IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RelaBigGiftView.this.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                        }
                    });
                }
            });

            videoPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer iMediaPlayer, int what, final int extra) {
                    if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RelaBigGiftView.this.setVideoRotation(extra);
                            }
                        });
                    }
                    return true;
                }
            });
        }

        private void reloadVideo(String url) {
            if (!TextUtils.isEmpty(url)) {
                /**
                 * v1.5.1或更新版本，flushBuffer默认值为TRUE，会flush播放器旧有数据
                 * @param url 播放地址
                 * @param flushBuffer 是否清除播放器的旧数据
                 * @param reloadMode reload模式
                 */
                // 选择该模式能更快速的reload
                // 可能存在的问题是在音视频交织不正常的情况下，可能忽略某路流
                //KSY_RELOAD_MODE_FAST,
                // 选择该模式reload速度可能较慢，但是更为精确
                //KSY_RELOAD_MODE_ACCURATE
                if (videoPlayer != null)
                    videoPlayer.reload(url, true, KSYMediaPlayer.KSYReloadMode.KSY_RELOAD_MODE_FAST);
            }
        }

        private void onResume() {
            if (videoPlayer != null && !videoPlayer.isPlaying())
                videoPlayer.start();
        }

        private void onPause() {
            if (videoPlayer != null && videoPlayer.isPlaying())
                videoPlayer.pause();
        }

        private void releaseMediaPlayer() {
            if (videoPlayer != null) videoPlayer.release();
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
                gpuImageFilter = generateGPUImageFilter();
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
         * Does not clean EglCore.
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
            if (videoPlayer != null) videoPlayer.setSurface(new Surface(mCameraTexture));
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

    }

    float[] mSTMatrix = new float[16];

    private GPUImageFilterGroup generateGPUImageFilter() {
        GPUImageFilterGroup gpuImageFilter = new GPUImageFilterGroup();
        GPUImageExtRotationTexFilter ext = new GPUImageExtRotationTexFilter();
        ext.setTexMatrix(mSTMatrix);
        gpuImageFilter.addFilter(ext);
        gpuImageFilter.addFilter(new GPUImageFilter("" +
                "attribute vec4 position;\n" +
                "attribute vec4 inputTextureCoordinate;\n" +
                " \n" +
                "varying vec2 textureCoordinate;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "    gl_Position = position;\n" +
                "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                "}", "" +
                "varying highp vec2 textureCoordinate;\n" +
                " \n" +
                "uniform sampler2D inputImageTexture;\n" +
                " \n" +
                "void main()\n" +
                "{\n" +
                "highp vec2 st = textureCoordinate;\n" +
                "highp vec2 lst = st * vec2(0.5, 1.0);\n" +
                "highp vec2 rst = lst + vec2(0.5, 0.0);\n" +
                "highp vec4 imgOrigin = texture2D(inputImageTexture, lst);\n" +
                "highp vec4 imgMask = texture2D(inputImageTexture, rst);\n" +
                "highp vec3 imgRGB = imgOrigin.rgb;\n" +
                "highp float imgAlpha = imgMask.r;\n" +
                "gl_FragColor = vec4(imgRGB.r,imgRGB.g,imgRGB.b, imgAlpha);\n" +
                "}"));
        return gpuImageFilter;
    }
}
