package video.com.relavideolibrary.camera;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.IntDef;

/**
 * Created by chad
 * Time 18/7/24
 * Email: wuxianchuang@foxmail.com
 * Description: Android 5.0 相机的API发生很大的变化。一些类屏蔽掉了 api的变化。相机的操作和功能，抽象剥离出来。
 */
public interface ICameraController {

    String TAG = "ICameraController";

    int CAMERA_FACING_BACK = 0;

    int CAMERA_FACING_FRONT = 1;

    int CAMERA_USB = 2;

    @IntDef({CAMERA_FACING_FRONT, CAMERA_FACING_BACK, CAMERA_USB})
    @interface CameraFacing {

    }

    /**
     * 垂直方向
     */
    int ORIENTATION_PORTRAIT = 0;
    /**
     * 水平方向
     */
    int ORIENTATION_HORIZONTAL = 1;
    /**
     * 水平翻转方向
     */
    int ORIENTATION_INVERT = 2;

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_HORIZONTAL, ORIENTATION_INVERT})
    @interface Orientation {

    }

    /**
     * open the camera
     */
    void open(int cameraId);

    /**
     * 手动聚焦
     *
     * @param point 触屏坐标 必须传入转换后的坐标
     */
    void onFocus(Point point, Camera.AutoFocusCallback callback);

    void setPreviewTexture(SurfaceTexture texture);

    void setOnPreviewFrameCallback(PreviewFrameCallback callback);

    void preview();

    Config.Size getPreviewSize();

    /**
     * close the camera
     */
    boolean close();

    void destroy();

    /**
     * 设置水平方向
     *
     * @param displayOrientation 参数值见 {@link Orientation}
     */
    void setDisplayOrientation(@Orientation int displayOrientation);

    class Config {
        public float rate = 1.778f; //宽高比
        public int minPreviewWidth;
        public int minPictureWidth;
        public Size size = new Size();

        public class Size {
            public int width;
            public int height;
        }
    }

    interface PreviewFrameCallback {
        void onPreviewFrame(byte[] bytes, int rotation, int width, int height);
    }

    interface PermissionCallback {
        boolean onRequestPermission();
    }
}
