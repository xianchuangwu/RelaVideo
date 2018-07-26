package video.com.relavideolibrary.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import video.com.relavideolibrary.Utils.Constant;

/**
 * Created by chad
 * Time 18/7/24
 * Email: wuxianchuang@foxmail.com
 * Description: 5.0以上相机封装
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Controller implements ICameraController {

    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    /**
     * 闪光灯关
     */
    private static final int FLASH_MODE_OFF = 0;
    /**
     * 闪光灯开
     */
    private static final int FLASH_MODE_TORCH = 1;
    /**
     * 闪光灯自动
     */
    private static final int FLASH_MODE_AUTO = 2;

    @IntDef({FLASH_MODE_TORCH, FLASH_MODE_OFF, FLASH_MODE_AUTO})
    @interface FlashMode {

    }

    private Range<Integer>[] fpsRanges = null;

    private int flashMode = FLASH_MODE_OFF;

    private Context mContext;

    private HandlerThread mCameraThread;

    private Handler mCameraHandler;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Config.Size mPreviewSize;

    private int width = Constant.VideoConfig.WIDTH, height = Constant.VideoConfig.HEIGHT;

    private SurfaceTexture mSurfaceTexture;

    private Semaphore cameraLock = new Semaphore(1);

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private PreviewFrameCallback frameCallback;

    public Camera2Controller(Context context) {
        this.mContext = context;
        startCameraThread();
    }

    @Override
    public void open(final int cameraId) {

        openCamera(cameraId);
    }

    @Override
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {

    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        this.mSurfaceTexture = texture;
    }

    @Override
    public void setOnPreviewFrameCallback(PreviewFrameCallback callback) {
        this.frameCallback = callback;
    }

    @Override
    public void preview() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                createCameraPreviewSession();
            }
        }, 500);
    }

    @Override
    public Config.Size getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public boolean close() {
        try {
            cameraLock.acquire();
            if (null != mCameraCaptureSession) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraLock.release();
        }
        return false;
    }

    @Override
    public void destroy() {
        stopBackgroundThread();
    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
    }

    public void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mCameraThread != null) {
            mCameraThread.quitSafely();
            mCameraThread = null;
            mCameraHandler = null;
        }
    }

    private void openCamera(int cameraId) {
        // 6.0+的系统需要检查系统权限 。
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //获取摄像头管理者，它主要用来查询和打开可用的摄像头
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        boolean cameraIdEnable = false;
        try {
            //遍历所有摄像头
            for (String id : cameraManager.getCameraIdList()) {
                //获取此ID对应摄像头的参数
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (facing != null && facing == cameraId && map != null) {
                    cameraIdEnable = true;
                    //根据屏幕尺寸（通过参数传进来）匹配最合适的预览尺寸
                    Size size1 = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                    Config config = new Config();
                    config.size.width = size1.getWidth();
                    config.size.height = size1.getHeight();
                    mPreviewSize = config.size;

                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (!cameraIdEnable) return;

        try {
            if (!cameraLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            //开启相机，第一个参数指示打开哪个摄像头，第二个参数mStateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraManager.openCamera(String.valueOf(cameraId), mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    //当相机成功打开后会回调onOpened方法，这里可以拿到CameraDevice对象，也就是具体的摄像头设备
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraLock.release();
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraLock.release();
            camera.close();
            mCameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        //设置SurfaceTexture的默认尺寸
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.width, mPreviewSize.height);
        //根据mSurfaceTexture创建Surface
        Surface surface = new Surface(mSurfaceTexture);
        try {
            //创建preview捕获请求
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //将此请求输出目标设为我们创建的Surface对象，这个Surface对象也必须添加给createCaptureSession才行
            mCaptureRequestBuilder.addTarget(surface);
            // 自动对焦
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //FPS
            if (fpsRanges != null) {
                Integer minFps = fpsRanges[0].getLower();
                //部分国产机型，fps值跟camera1的一样
                int fps = minFps < 100 ? 15 : 15000;
                int selectIndex = 0;
                for (int i = 0; i < fpsRanges.length; i++) {

                    if (fpsRanges[i].getLower() >= fps && minFps > fpsRanges[i].getLower()) {
                        minFps = fpsRanges[i].getLower();
                        selectIndex = i;
                    }
                }
                Log.d("fpsRanges", "min: " + fpsRanges[selectIndex].getLower() + ",max: " + fpsRanges[selectIndex].getUpper());
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRanges[selectIndex]);
            }
            //闪光灯
            updateFlashMode(Camera2Controller.this.flashMode, mCaptureRequestBuilder);

            //创建捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    // The camera is already closed
                    if (null == mCameraDevice) {
                        return;
                    }
                    try {
                        //创建捕获请求
                        mCaptureRequest = mCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        //设置重复捕获数据的请求，之后surface绑定的SurfaceTexture中就会一直有数据到达，然后就会回调SurfaceTexture.OnFrameAvailableListener接口
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(mContext, "Camera2 参数配置错误", Toast.LENGTH_SHORT).show();
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateFlashMode(@FlashMode int flashMode, CaptureRequest.Builder builder) {
        switch (flashMode) {
            case FLASH_MODE_TORCH:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
            case FLASH_MODE_OFF:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case FLASH_MODE_AUTO:
            default:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                break;
        }
    }

    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }
}
