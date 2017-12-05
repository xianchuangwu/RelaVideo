package video.com.relavideolibrary.Utils;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by chad
 * Time 17/12/5
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class CameraHelper {

    public static final String TAG = "CameraHelper";

    //升序
    private static CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();

    /**
     * 获取所有支持的预览尺寸,优先返回1280*720
     *
     * @return
     */
    public static Camera.Size getSupportPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        int minWidth = 2000;
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.d(TAG, "SupportPreviewSize: " + s.width + "/" + s.height);
            if (s.width == Constant.WIDTH && s.height == Constant.HEIGHT) return s;
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
     * @return
     */
    public static Camera.Size getSupportVideoSize(Camera.Parameters parameters) {
        List<Camera.Size> list = null;

        //在部分手机上getSupportedVideoSizes为null,华为手机会出现这种情况
        if (parameters.getSupportedVideoSizes() != null) {
            Log.d(TAG, "has multi getSupportedVideoSizes");
            list = parameters.getSupportedVideoSizes();
        } else {
            Log.d(TAG, "has no multi getSupportedVideoSizes, use getSupportedPreviewSizes");
            // Video sizes may be null, which indicates that all the supported
            // preview sizes are supported for video recording.
            list = parameters.getSupportedPreviewSizes();
        }
        int minHeight = 2000;
        Collections.sort(list, ascendSizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.d(TAG, "SupportVideoSize: " + s.width + "/" + s.height);
            if (s.width == Constant.WIDTH && s.height == Constant.HEIGHT) return s;
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
     * andoid系统默认预览都是横着的，直接camera.setDisplayOrientation(90)也不行，因为有的手机对系统底层进行了矫正
     * 该方法获取正确旋转的角度，适配所有机型的相机硬件预览方案
     *
     * @param cameraId
     * @param camera
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
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
     * 获取相机旋转的角度
     * 无论是录制视频还是拍照,视频的角度和照片的角度都要根据相机的角度旋转
     * 视频的角度可以在录制的时候设置mediaRecorder.setOrientationHint,拍照只能在回调中处理bitmap
     *
     * @param cameraId
     * @return
     */
    public static int getCameraOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    //升序
    public static class CameraAscendSizeComparator implements Comparator<Camera.Size> {
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
