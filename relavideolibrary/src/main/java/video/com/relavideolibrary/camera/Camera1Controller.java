package video.com.relavideolibrary.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.camera.utils.Constants;

/**
 * Created by chad
 * Time 18/7/24
 * Email: wuxianchuang@foxmail.com
 * Description: 5.0以下相机API的封装
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Camera1Controller implements ICameraController {

    private Context mContext;
    /**
     * 相机的宽高及比例配置
     */
    private ICameraController.Config mConfig;
    /**
     * 相机实体
     */
    private Camera mCamera;

    /**
     * 屏幕旋转角度
     */
    private int displayOrientation;
    /**
     * 相机旋转角度
     */
    private int cameraOrientation = 0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private Config.Size mPreSize;

    public Camera1Controller(Context context) {
        this.mContext = context;
        /**初始化一个默认的格式大小*/
        mConfig = new ICameraController.Config();
        mConfig.minPreviewWidth = Constant.VideoConfig.WIDTH;
        mConfig.minPictureWidth = Constant.VideoConfig.WIDTH;
        mConfig.rate = 1.778f;
    }

    @Override
    public void open(@CameraFacing int cameraId) {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mCamera = Camera.open(cameraId);
        if (mCamera != null) {
            /**选择当前设备允许的预览尺寸*/
            Camera.Parameters param = mCamera.getParameters();
            //默认对焦方式 FOCUS_MODE_CONTINUOUS_VIDEO效果相对好些
            List<String> focusModes = param.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId);
            mCamera.setDisplayOrientation(rotation);
            cameraOrientation = rotation;
            if (displayOrientation == Surface.ROTATION_0) {
                if (cameraOrientation == 90 || cameraOrientation == 270) {
                    cameraOrientation = (cameraOrientation + 180) % 360;
                }
            }

            Camera.Size preSize = getPropPreviewSize(param.getSupportedPreviewSizes(), mConfig.rate,
                    mConfig.minPreviewWidth);
            Camera.Size picSize = getPropPictureSize(param.getSupportedPictureSizes(), mConfig.rate,
                    mConfig.minPictureWidth);

            param.setPreviewSize(preSize.width, preSize.height);
            param.setPictureSize(picSize.width, picSize.height);

            List<int[]> a = param.getSupportedPreviewFpsRange();
            int minFps = a.get(0)[0];
            int maxFps = a.get(0)[1];
            for (int i = 0; i < a.size(); i++) {
                int fps = a.get(i)[0];
                if (fps >= 15000 && minFps > fps) {
                    minFps = fps;
                    maxFps = a.get(i)[1];
                }
            }
            param.setPreviewFpsRange(minFps, maxFps);

            mCamera.setParameters(param);
            Camera.Size size = mCamera.getParameters().getPreviewSize();
            Config config = new Config();
            config.size.width = size.width;
            config.size.height = size.height;
            mPreSize = config.size;
        }
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                Log.e(TAG, "----setPreviewTexture");
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setOnPreviewFrameCallback(final PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.addCallbackBuffer(new byte[mPreSize.width * mPreSize.height * 3 / 2]);
            mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data, cameraOrientation, mPreSize.width, mPreSize.height);
                    mCamera.addCallbackBuffer(data);
                }
            });
//            mCameraController.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    callback.onPreviewFrame(data, cameraOrientation, mPreSize.width, mPreSize.height);
//                }
//            });
        }
    }

    @Override
    public void preview() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    @Override
    public Config.Size getPreviewSize() {
        return mPreSize;
    }

    @Override
    public boolean close() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void setDisplayOrientation(int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    @Override
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        Camera.Parameters parameters = mCamera.getParameters();
        boolean supportFocus = true;
        boolean supportMetering = true;
        //不支持设置自定义聚焦，则使用自动聚焦，返回
        if (parameters.getMaxNumFocusAreas() <= 0) {
            supportFocus = false;
        }
        if (parameters.getMaxNumMeteringAreas() <= 0) {
            supportMetering = false;
        }
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areas1 = new ArrayList<Camera.Area>();
        //再次进行转换
        point.x = (int) (((float) point.x) / Constants.getInstance().getScreenWidth() * 2000 - 1000);
        point.y = (int) (((float) point.y) / Constants.getInstance().getScreenHeight() * 2000 - 1000);

        int left = point.x - 300;
        int top = point.y - 300;
        int right = point.x + 300;
        int bottom = point.y + 300;
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        areas1.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        if (supportFocus) {
            parameters.setFocusAreas(areas);
        }
        if (supportMetering) {
            parameters.setMeteringAreas(areas1);
        }

        try {
            mCamera.setParameters(parameters);// 部分手机 会出Exception（红米）
            mCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getCameraDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }

        return rotation;
    }

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };
}
