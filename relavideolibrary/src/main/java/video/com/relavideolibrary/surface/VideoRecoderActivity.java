package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.CameraManager;
import video.com.relavideolibrary.Utils.Constant;

public class VideoRecoderActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 只能通过此方法进入VideoRecoderActivity
     *
     * @param context      启动activity需要上下文
     * @param cameraParams 初始化相机参数，不能为空
     */
    public static void startActivity(@NonNull Context context, @NonNull String cameraParams) {
        Intent intent = new Intent(context, VideoRecoderActivity.class);
        intent.putExtra(Constant.KEY_INTENT_CAMERA_PARAMS, cameraParams);
        ((Activity) context).startActivityForResult(intent, Constant.REQUEST_START_RECODER);
    }

    private ImageView recoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_recoder);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
        findViewById(R.id.camera_switch).setOnClickListener(this);
        recoder = (ImageView) findViewById(R.id.recoder);
        recoder.setOnClickListener(this);

        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        if (getIntent() != null) {
            CameraManager.CameraParams cameraParams = CameraManager.CameraParams.fromJson(getIntent().getStringExtra(Constant.KEY_INTENT_CAMERA_PARAMS));
            CameraManager.getInstance().openCamera(null, mSurfaceView);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel) {
            finish();
        } else if (i == R.id.next) {
            Toast.makeText(this, "opening", Toast.LENGTH_SHORT).show();
        } else if (i == R.id.camera_switch) {
            CameraManager.getInstance().switchCamera();
        } else if (i == R.id.recoder) {
            CameraManager.getInstance().recoder();
        }
    }
}
