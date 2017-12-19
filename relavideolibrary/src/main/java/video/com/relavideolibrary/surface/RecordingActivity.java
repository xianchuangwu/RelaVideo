package video.com.relavideolibrary.surface;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.filter.GPUImageBeautifyFilter;
import video.com.relavideolibrary.view.RoundCornersImageView;

public class RecordingActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "RecordingActivity";

    private RoundCornersImageView gallery;

    private ImageView recording;

    private ImageView beautiful_icon;

    //是否开启美颜
    private boolean isBeautiful = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recording);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        beautiful_icon = findViewById(R.id.beautiful_icon);
        gallery = findViewById(R.id.gallery);
        recording = findViewById(R.id.recording);
        gallery.setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.camera_switch).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.beautiful).setOnClickListener(this);

        setup();

    }

    private void setup() {
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        RelativeLayout video_Container = findViewById(R.id.glSurfaceView_container);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        video_Container.addView(glSurfaceView, layoutParams);

        glSurfaceView.setEGLContextClientVersion(2);

        GPUImage gpuImage = new GPUImage(this);
        gpuImage.setGLSurfaceView(glSurfaceView);

        gpuImage.setFilter(new GPUImageBeautifyFilter());

        Camera mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

        Camera.Parameters parameters = mCamera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        //碎片化原因,没有很好的对焦方案 FOCUS_MODE_CONTINUOUS_VIDEO效果相对好些
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPictureSizes();
        Log.d("preview",supportedPreviewSizes.toString());

        parameters.setPreviewSize(320, 240);

        mCamera.setParameters(parameters);

        gpuImage.setUpCamera(mCamera);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.next) {
            Toast.makeText(this, "opening", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.camera_switch) {

        } else if (id == R.id.gallery) {
            startActivityForResult(new Intent(this, GalleryActivity.class), Constant.IntentCode.REQUEST_CODE_GALLERY);
        } else if (id == R.id.delete) {

        } else if (id == R.id.beautiful) {
            if (isBeautiful) {
                beautiful_icon.setImageResource(R.mipmap.ic_beautiful);
            } else {
                beautiful_icon.setImageResource(R.mipmap.ic_beautiful_selected);
            }
            isBeautiful = !isBeautiful;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IntentCode.REQUEST_CODE_GALLERY && resultCode == Constant.IntentCode.RESULT_CODE_GALLERY_OK) {
            startActivity(new Intent(this, EditActivity.class));
        }
    }

}
