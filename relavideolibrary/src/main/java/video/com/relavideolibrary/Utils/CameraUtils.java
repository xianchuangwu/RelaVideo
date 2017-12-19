package video.com.relavideolibrary.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by chad
 * Time 17/3/7
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class CameraUtils {

    public static final String TAG = "CameraUtils";

    public static final int FIRST_WIDTH = 1280;
    public static final int FIRST_HEIGHT = 720;

    //降序
    private CameraDropSizeComparator dropSizeComparator = new CameraDropSizeComparator();
    //升序
    private CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();
    private static CameraUtils myCamPara = null;

    private CameraUtils() {
    }

    public static CameraUtils getInstance() {
        if (myCamPara == null) {
            myCamPara = new CameraUtils();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    /**
     * 默认打开后置摄像头
     *
     * @return
     */
    public Camera getBackCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            throw new RuntimeException("camera.open() error:" + e.getMessage());
        }
        return camera;
    }

    /**
     * 根据传入id打开对应摄像头
     *
     * @return
     */
    public Camera getCameraById(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            throw new RuntimeException("camera.open() error:" + e.getMessage());
        }
        return camera;
    }

    /**
     * 打开闪关灯
     *
     * @param mCamera
     */
    public void turnLightOn(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }

    /**
     * 自动模式闪光灯
     *
     * @param mCamera
     */
    public void turnLightAuto(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }

    /**
     * 关闭闪光灯
     *
     * @param mCamera
     */
    public void turnLightOff(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        // Check if camera flash exists
        if (flashModes == null) {
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // Turn off the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }

    /**
     * andoid系统默认预览都是横着的，直接camera.setDisplayOrientation(90)也不行，因为有的手机对系统底层进行了矫正
     * 该方法获取正确旋转的角度，适配所有机型的相机硬件预览方案
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 把相机拍照返回照片转正
     *
     * @return bitmap 图片
     */
    public Bitmap rotateTakePictureBitmap(int cameraId, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(getCameraOrientation(cameraId));
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 获取相机旋转的角度
     * 无论是录制视频还是拍照,视频的角度和照片的角度都要根据相机的角度旋转
     * 视频的角度可以在录制的时候设置mediaRecorder.setOrientationHint,拍照只能在回调中处理bitmap
     *
     * @param cameraId
     * @return
     */
    public int getCameraOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }
    ///////////////////////////////////////////////预览尺寸,拍照尺寸和录制视频尺寸必须要是当前手机支持的尺寸/////////////////////////////////////////////////

    /**
     * 获取所有支持的预览尺寸,优先返回1280*720
     *
     * @param list
     * @return
     */
    public Camera.Size getSupportPreviewSize(List<Camera.Size> list) {
        int minWidth = 2000;
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.d(TAG, "SupportPreviewSize: " + s.width + "/" + s.height);
            if (s.width == FIRST_WIDTH && s.height == FIRST_HEIGHT) return s;
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 获取手机所有支持的录制视频尺寸 一般有1920x1088 1280x720 800x480 768x432 720x480 640x480 576x432 480x320
     * 384x288 352x288 320x240 240x160 176x144 优先返回1280*720
     *
     * @param list
     * @return
     */
    public Camera.Size getSupportVideoSize(List<Camera.Size> list) {
        int minHeight = 2000;
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.d(TAG, "SupportVideoSize: " + s.width + "/" + s.height);
            if (s.width == FIRST_WIDTH && s.height == FIRST_HEIGHT) return s;
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的拍照尺寸,优先返回1280*720
     *
     * @param list
     * @return
     */
    public Camera.Size getSupportPictureSize(List<Camera.Size> list) {
        int minWidth = 2000;
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.d(TAG, "SupportPictureSize: " + s.width + "/" + s.height);
            if (s.width == FIRST_WIDTH && s.height == FIRST_HEIGHT) return s;
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    //降序
    public class CameraDropSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    //升序
    public class CameraAscendSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
