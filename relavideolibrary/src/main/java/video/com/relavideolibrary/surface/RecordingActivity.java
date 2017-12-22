package video.com.relavideolibrary.surface;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.camera.CameraView;
import video.com.relavideolibrary.camera.utils.Constants;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.VideoBean;
import video.com.relavideolibrary.view.RecordingButton;
import video.com.relavideolibrary.view.RoundCornersImageView;

public class RecordingActivity extends BaseActivity implements View.OnClickListener, RecordingButton.OnRecordingListener {

    public static final String TAG = "RecordingActivity";

    private RoundCornersImageView gallery;

    private RecordingButton recording;

    private ImageView beautiful_icon;

    private TextView next;

    //是否开启美颜
    private boolean isBeautiful = false;
    private ExecutorService executorService;
    private CameraView cameraView;

    private String outputPath;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recording);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        cameraView = findViewById(R.id.cameraView);
        beautiful_icon = findViewById(R.id.beautiful_icon);
        gallery = findViewById(R.id.gallery);
        recording = findViewById(R.id.recording);
//        recording.setOnClickListener(this);
        recording.registerOnClickListener(this);
        gallery.setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        findViewById(R.id.camera_switch).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.beautiful).setOnClickListener(this);

        Constants.getInstance().setContext(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.next) {
            startActivity(new Intent(this, EditActivity.class));
        } else if (id == R.id.camera_switch) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    cameraView.switchCamera();
                }
            });
        } else if (id == R.id.gallery) {
            startActivityForResult(new Intent(this, GalleryActivity.class), Constant.IntentCode.REQUEST_CODE_GALLERY);
        } else if (id == R.id.delete) {

        } else if (id == R.id.beautiful) {
            if (isBeautiful) {
                beautiful_icon.setImageResource(R.mipmap.ic_beautiful);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.changeBeautyLevel(0);
                    }
                });
            } else {
                beautiful_icon.setImageResource(R.mipmap.ic_beautiful_selected);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.changeBeautyLevel(5);
                    }
                });
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

    @Override
    public void startRecording() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                outputPath = "/storage/emulated/0/rela_" + SystemClock.currentThreadTimeMillis() + ".mp4";
                cameraView.setSavePath(outputPath);
                cameraView.startRecord();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        next.setAlpha(0.5f);
                        next.setEnabled(false);
                    }
                });
            }
        });

    }

    @Override
    public void stopRecording() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                cameraView.stopRecord();
                VideoBean bean = new VideoBean();
                bean.videoPath = outputPath;
                VideoManager.getInstance().setVideoBean(bean);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        next.setAlpha(1.0f);
                        next.setEnabled(true);
                    }
                });
            }
        });

    }
}
