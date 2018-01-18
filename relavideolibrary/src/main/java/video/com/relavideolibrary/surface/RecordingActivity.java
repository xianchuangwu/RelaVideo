package video.com.relavideolibrary.surface;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import video.com.relavideolibrary.Utils.codec.MulitVideoMuxer;
import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.Utils.CornerTransformation;
import video.com.relavideolibrary.Utils.FileManager;
import video.com.relavideolibrary.Utils.ScreenUtils;
import video.com.relavideolibrary.camera.CameraView;
import video.com.relavideolibrary.camera.utils.Constants;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.model.VideoBean;
import video.com.relavideolibrary.view.RecordingButton;
import video.com.relavideolibrary.view.RecordingLine;

public class RecordingActivity extends BaseActivity implements View.OnClickListener
        , RecordingButton.OnRecordingListener
        , RecordingLine.RecordingLineListener
        , MulitVideoMuxer.MediaMuxerListener {

    public static final String TAG = "RecordingActivity";

    private ImageView gallery;

    private RecordingButton recording;

    private ImageView beautiful_icon;

    private RelativeLayout gallery_container;

    private TextView next;

    private TextView second_txt;

    private RecordingLine recordingLine;

    private RelativeLayout delete;

    //是否开启美颜
    private boolean isBeautiful = false;
    private ExecutorService executorService;
    private CameraView cameraView;

    private String cameraOutputPath;

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

        gallery_container = findViewById(R.id.gallery_container);
        cameraView = findViewById(R.id.cameraView);
        beautiful_icon = findViewById(R.id.beautiful_icon);
        recordingLine = findViewById(R.id.recordingLine);
        recordingLine.setRecordingLineListener(this);
        gallery = findViewById(R.id.gallery);
        recording = findViewById(R.id.recording);
        recording.registerOnClickListener(this);
        gallery.setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        second_txt = findViewById(R.id.second_txt);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        findViewById(R.id.camera_switch).setOnClickListener(this);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(this);
        findViewById(R.id.beautiful).setOnClickListener(this);

        Constants.getInstance().setContext(getApplicationContext());
        executorService = Executors.newSingleThreadExecutor();

        initFirstGalleryVideo();
    }

    private void initFirstGalleryVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> videos = new ArrayList<>();
                ContentResolver resolver = RecordingActivity.this.getContentResolver();
                Cursor cursor = null;
                try {
                    //查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
                    cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            videos.add(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                if (videos.size() > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(RecordingActivity.this)
                                    .load(videos.get(videos.size() - 1))
                                    .transform(new CenterCrop(RecordingActivity.this)
                                            , new CornerTransformation(RecordingActivity.this, 10))
                                    .placeholder(R.mipmap.ic_default)
                                    .into(gallery);
                        }
                    });
                }
            }
        }).start();
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
            ScreenUtils.preventViewMultipleTouch(v, 2000);
            Log.d(TAG, "outputList size :" + outputList.size());
            if (outputList.size() > 0) {
                showDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new MulitVideoMuxer(outputList).start(RecordingActivity.this);

                    }
                }).start();
            }
        } else if (id == R.id.camera_switch) {
            ScreenUtils.preventViewMultipleTouch(v, 2000);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    cameraView.switchCamera();
                }
            });
        } else if (id == R.id.gallery) {
            ScreenUtils.preventViewMultipleTouch(v, 2000);
            startActivityForResult(new Intent(this, GalleryActivity.class), Constant.IntentCode.REQUEST_CODE_GALLERY);
        } else if (id == R.id.delete) {
            if (recordingLine.delete()) {
                outputList.remove(outputList.size() - 1);
                Log.d(TAG, "outputList size :" + outputList.size());
                if (outputList.size() == 0) {
                    next.setAlpha(0.5f);
                    next.setEnabled(false);
                    delete.setAlpha(0.5f);
                    delete.setEnabled(false);
                    gallery_container.setAlpha(1.0f);
                    gallery.setEnabled(true);
                }
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
                        cameraView.changeBeautyLevel(1);
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
        } else if (requestCode == Constant.IntentCode.REQUEST_CODE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getExtras() != null) {
                    Intent intent = getIntent();
                    intent.putExtras(data.getExtras());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Log.e(TAG, "intent data null");
                }
            }

            //从编辑页回来，清空视频管理类
            VideoManager.getInstance().clean();
        }
    }

    @Override
    public void startRecording() {
        if (recordingLine.getLastDuration() == Constant.VideoConfig.MAX_VIDEO_DURATION) {
            Toast.makeText(this, getString(R.string.video_max_duration), Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                cameraOutputPath = FileManager.getVideoFile();
                cameraView.setSavePath(cameraOutputPath);
                cameraView.startRecord();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        next.setAlpha(0.5f);
                        next.setEnabled(false);
                        delete.setAlpha(0.5f);
                        delete.setEnabled(false);
                        gallery_container.setAlpha(0.5f);
                        gallery.setEnabled(false);
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
                outputList.add(cameraOutputPath);
                Log.d(TAG, "outputList size :" + outputList.size());
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

    @Override
    public void videoMergeSuccess(String cameraOutputPath) {
        Log.d(TAG, "merge path :" + cameraOutputPath);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            }
        });
        VideoBean bean = new VideoBean();
        bean.videoPath = cameraOutputPath;
        VideoManager.getInstance().setVideoBean(bean);
        startActivityForResult(new Intent(RecordingActivity.this, EditActivity.class), Constant.IntentCode.REQUEST_CODE_EDIT);
    }

    @Override
    public void videoMergeError(String messge) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissDialog();
                Toast.makeText(RecordingActivity.this, "merge video fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void recordProgress(long progress) {
        @SuppressLint("DefaultLocale") String result = String.format("%.1f", (float) progress / 1000);
        second_txt.setText(result + " S");
    }
}
