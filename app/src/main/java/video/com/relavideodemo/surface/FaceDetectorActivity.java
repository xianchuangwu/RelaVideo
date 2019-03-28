package video.com.relavideodemo.surface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.thel.R;

import video.com.relavideolibrary.camera.CameraView;

public class FaceDetectorActivity extends AppCompatActivity {

    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_face_detector);
        cameraView = findViewById(R.id.cameraView);
        cameraView.setOnEGLContextHandler(new CameraView.OnEGLContextListener() {
            @Override
            public void onEGLContextReady() {
                cameraView.switchCamera();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.onDestroy();
    }
}
