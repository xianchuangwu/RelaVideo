package video.com.relavideolibrary.surface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.camera.CameraView;
import video.com.relavideolibrary.camera.utils.Constants;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.VideoBean;
import video.com.relavideolibrary.videotrimmer.utils.Mp4parserUtils;
import video.com.relavideolibrary.view.RecordingButton;
import video.com.relavideolibrary.view.RecordingLine;
import video.com.relavideolibrary.view.RoundCornersImageView;

public class RecordingActivity extends BaseActivity implements View.OnClickListener, RecordingButton.OnRecordingListener {

    public static final String TAG = "RecordingActivity";

    private RoundCornersImageView gallery;

    private RecordingButton recording;

    private ImageView beautiful_icon;

    private TextView next;

    private RecordingLine recordingLine;

    private RelativeLayout delete;

    //是否开启美颜
    private boolean isBeautiful = false;
    private ExecutorService executorService;
    private CameraView cameraView;

    private String outputPath;

    private List<String> outputList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_recording);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        getWindow().setAttributes(params);

        cameraView = findViewById(R.id.cameraView);
        beautiful_icon = findViewById(R.id.beautiful_icon);
        recordingLine = findViewById(R.id.recordingLine);
        gallery = findViewById(R.id.gallery);
        recording = findViewById(R.id.recording);
        recording.registerOnClickListener(this);
        gallery.setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        findViewById(R.id.camera_switch).setOnClickListener(this);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(this);
        findViewById(R.id.beautiful).setOnClickListener(this);

        Constants.getInstance().setContext(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            finish();
        } else if (id == R.id.next) {
            if (outputList.size() > 0) {
                String[] outputArr = new String[outputList.size()];
                outputList.toArray(outputArr);
                final String output = "/storage/emulated/0/rela_" + SystemClock.currentThreadTimeMillis() + ".mp4";
                showDialog();
                Mp4parserUtils.videoMerge(outputArr, output, new Mp4parserUtils.MergeVideoListener() {
                    @Override
                    public void success() {
                        dismissDialog();
                        VideoBean bean = new VideoBean();
                        bean.videoPath = output;
                        VideoManager.getInstance().setVideoBean(bean);
                        startActivityForResult(new Intent(RecordingActivity.this, EditActivity.class), Constant.IntentCode.REQUEST_CODE_EDIT);
                    }

                    @Override
                    public void failed() {
                        dismissDialog();
                        Toast.makeText(RecordingActivity.this, "merge video fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
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
            if (recordingLine.delete()) {
                outputList.remove(outputList.size() - 1);
            }
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
            startActivityForResult(new Intent(this, EditActivity.class), Constant.IntentCode.REQUEST_CODE_EDIT);
        } else if (requestCode == Constant.IntentCode.REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            if (data.getExtras() != null) {
                Intent intent = getIntent();
                intent.putExtras(data.getExtras());
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                Log.e(TAG, "intent data null");
            }
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
                        delete.setAlpha(0.5f);
                        delete.setEnabled(false);
                        recordingLine.start();
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
                outputList.add(outputPath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recordingLine.getLastDuration() > Constant.VideoConfig.MIN_VIDEO_DURATION) {
                            next.setAlpha(1.0f);
                            next.setEnabled(true);
                        }
                        delete.setAlpha(1.0f);
                        delete.setEnabled(true);
                        recordingLine.stop();
                    }
                });
            }
        });

    }
}
