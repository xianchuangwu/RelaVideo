package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.videotrimmer.K4LVideoTrimmer;
import video.com.relavideolibrary.videotrimmer.interfaces.OnK4LVideoListener;
import video.com.relavideolibrary.videotrimmer.interfaces.OnTrimVideoListener;

public class CutActivity extends BaseActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private K4LVideoTrimmer mVideoTrimmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0修改状态栏字体色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.0 以上直接设置状态栏颜色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white_10_alpha));
            }
        }
        setContentView(R.layout.activity_cut);
        showTranslucentView();

        mVideoTrimmer = ((K4LVideoTrimmer) findViewById(R.id.timeLine));
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnK4LVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(VideoManager.getInstance().getVideoBean().videoPath));
        }
    }

    @Override
    public void onTrimStarted() {
        showDialog();
    }

    @Override
    public void getResult(final Uri uri) {
        dismissDialog();
        VideoManager.getInstance().getVideoBean().videoPath = uri.getPath();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onError(final String message) {
        dismissDialog();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CutActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
    }
}
