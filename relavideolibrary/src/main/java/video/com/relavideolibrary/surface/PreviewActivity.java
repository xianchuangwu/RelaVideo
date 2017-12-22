package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksyun.media.player.IMediaPlayer;

import java.io.IOException;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import baidu.measure.IRenderView;
import baidu.measure.SurfaceRenderView;

public class PreviewActivity extends BaseActivity implements View.OnClickListener, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    private String path;

    private SurfaceRenderView surfaceRenderView;
    private MediaPlayer mediaPlayer;
    private ImageView play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.complete).setOnClickListener(this);
        play = findViewById(R.id.play);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        if (TextUtils.isEmpty(path)) finish();

        initVideoView();
    }

    private void initVideoView() {
        RelativeLayout video_container = findViewById(R.id.video_container);
        surfaceRenderView = new SurfaceRenderView(this);
        surfaceRenderView.setId(R.id.preview_video_id);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        video_container.addView(surfaceRenderView, layoutParams);
        surfaceRenderView.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        surfaceRenderView.getHolder().addCallback(this);
        surfaceRenderView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancel) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else if (id == R.id.complete) {
            setResult(Activity.RESULT_OK);
            finish();
        } else if (id == R.id.preview_video_id) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    play.setVisibility(View.VISIBLE);
                } else {
                    mediaPlayer.start();
                    play.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && play.getVisibility() == View.INVISIBLE)
            mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.setSurface(holder.getSurface());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(final MediaPlayer iMediaPlayer) {
        if (surfaceRenderView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    surfaceRenderView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                }
            });
        }
        mediaPlayer.start();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            if (surfaceRenderView != null)
                surfaceRenderView.setVideoRotation(extra);
        }
        return true;
    }

    @Override
    public void onVideoSizeChanged(final MediaPlayer iMediaPlayer, int width, int height) {
        if (surfaceRenderView != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    surfaceRenderView.setVideoSize(iMediaPlayer.getVideoWidth(), iMediaPlayer.getVideoHeight());
                }
            });
        }
    }
}
