package video.com.relavideolibrary.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.Locale;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import video.com.relavideolibrary.camera.renderer.CameraRenderer;


/**
 * Created by cj on 2017/8/1.
 * desc
 */

public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, ICamera.PreviewFrameCallback {
    private static final String LOG_TAG = "CameraView";

    private Context mContext;

    private EGLContext mEGLCurrentContext;

    protected CameraRenderer mCameraRenderer;
    protected CameraController mCamera;

    private int renderWidth = 0, renderHeight = 0;

    private boolean isSetParm = false;

    private int cameraId;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        /**初始化OpenGL的相关信息*/
        setEGLContextFactory(new MyContextFactory(this));
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        setCameraDistance(100);//相机距离

        /**初始化Camera的绘制类*/
        mCameraRenderer = new CameraRenderer(getResources());
        /**初始化相机的管理类*/
        mCamera = new CameraController();

    }

    private void open(int cameraId) {
        mCamera.close();
        mCamera.open(cameraId);
        mCameraRenderer.setCameraId(cameraId);
        final Camera.Size previewSize = mCamera.getPreviewSize();
        renderWidth = previewSize.height;
        renderHeight = previewSize.width;
        SurfaceTexture texture = mCameraRenderer.getTexture();
        texture.setOnFrameAvailableListener(this);
        mCamera.setOnPreviewFrameCallback(this);
        mCamera.setPreviewTexture(texture);
        mCamera.preview();
    }

    public void switchCamera() {
        cameraId = cameraId == 0 ? 1 : 0;
        open(cameraId);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraRenderer.onSurfaceCreated(gl, config);
        if (!isSetParm) {
            open(cameraId);
            stickerInit();
        }
        mCameraRenderer.setPreviewSize(renderWidth, renderHeight);
        if (mOnEGLContextHandler != null) {
            mOnEGLContextHandler.onEGLContextReady();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCameraRenderer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (isSetParm) {
            mCameraRenderer.onDrawFrame(gl);

            if (mOnFrameAvailableHandler != null) {
                mOnFrameAvailableHandler.onFrameAvailable(mEGLCurrentContext, mCameraRenderer.getPreviewTextureID(), renderWidth, renderHeight);
            }
        }
    }

    /**
     * 每次Activity onResume时被调用,第一次不会打开相机
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isSetParm) {
            open(cameraId);
        }
    }

    public void onDestroy() {
        if (mCamera != null) {
            mCamera.close();
            if (getParent() != null && getParent() instanceof ViewGroup) {
                ((ViewGroup) getParent()).removeView(this);
            }
        }
        mEGLCurrentContext = null;
    }

    /**
     * 摄像头聚焦
     */
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        mCamera.onFocus(point, callback);
    }

    public int getCameraId() {
        return cameraId;
    }

    public int getBeautyLevel() {
        return mCameraRenderer.getBeautyLevel();
    }

    public void changeBeautyLevel(final int level) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.changeBeautyLevel(level);
            }
        });
    }

    public void startRecord() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.startRecord();
            }
        });
    }

    public void stopRecord() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.stopRecord();
            }
        });
    }

    public void setSavePath(String path) {
        mCameraRenderer.setSavePath(path);
    }

    public void resume(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.onResume(auto);
            }
        });
    }

    public void pause(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.onPause(auto);
            }
        });
    }

    public void onTouch(final MotionEvent event) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRenderer.onTouch(event);
            }
        });
    }

    private void stickerInit() {
        if (!isSetParm && renderWidth > 0 && renderHeight > 0) {
            isSetParm = true;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    public void setOnEGLContextHandler(OnEGLContextListener listener) {
        this.mOnEGLContextHandler = listener;
    }

    public void setOnFrameAvailableHandler(OnFrameAvailableListener listener) {
        this.mOnFrameAvailableHandler = listener;
    }

    @Override
    public void onPreviewFrame(byte[] data, int rotation, int width, int height) {

    }

    public interface OnFrameAvailableListener {
        void onFrameAvailable(EGLContext eglContext, int textureId, int width, int height);
    }

    private OnFrameAvailableListener mOnFrameAvailableHandler;

    public interface OnEGLContextListener {
        void onEGLContextReady();
    }

    private OnEGLContextListener mOnEGLContextHandler;

    private static class MyContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        private CameraView mRenderer;

        public MyContextFactory(CameraView renderer) {
            Log.d(LOG_TAG, "MyContextFactory " + renderer);
            this.mRenderer = renderer;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.d(LOG_TAG, "createContext " + egl + " " + display + " " + eglConfig);
            checkEglError("before createContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};

            EGLContext ctx;

            if (mRenderer.mEGLCurrentContext == null) {
                mRenderer.mEGLCurrentContext = egl.eglCreateContext(display, eglConfig,
                        EGL10.EGL_NO_CONTEXT, attrib_list);
                ctx = mRenderer.mEGLCurrentContext;
            } else {
                ctx = mRenderer.mEGLCurrentContext;
            }
            checkEglError("after createContext", egl);
            return ctx;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            Log.d(LOG_TAG, "destroyContext " + egl + " " + display + " " + context + " " + mRenderer.mEGLCurrentContext);
            if (mRenderer.mEGLCurrentContext == null) {
                egl.eglDestroyContext(display, context);
            }
        }

        private static void checkEglError(String prompt, EGL10 egl) {
            int error;
            while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
                Log.d(LOG_TAG, String.format(Locale.US, "%s: EGL error: 0x%x", prompt, error));
            }
        }
    }
}
