package video.com.relavideodemo.surface;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;

import com.thel.R;

import baidu.facedetect.ArgbPool;
import baidu.facedetect.FaceDetectManager;
import baidu.facedetect.OnImageFrameAvailableListener;

public class TestActivity extends AppCompatActivity {

    private HandlerThread cameraHandlerThread = null;
    private Handler cameraHandler = null;
    private SurfaceView textureView;
    private Camera camera;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int displayOrientation;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private int preferredWidth = 1280;
    private int preferredHeight = 720;

    private ArgbPool argbPool = new ArgbPool();

    private OnImageFrameAvailableListener onImageFrameAvailableListener;

    private FaceDetectManager faceDetectManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }
}
