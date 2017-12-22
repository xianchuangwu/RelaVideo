package video.com.relavideolibrary.surface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import video.com.relavideolibrary.BaseActivity;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.manager.VideoManager;
import video.com.relavideolibrary.videotrimmer.K4LVideoTrimmer;
import video.com.relavideolibrary.videotrimmer.interfaces.OnK4LVideoListener;
import video.com.relavideolibrary.videotrimmer.interfaces.OnTrimVideoListener;

public class CutActivity extends BaseActivity implements OnTrimVideoListener, OnK4LVideoListener {

    private K4LVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        mVideoTrimmer = ((K4LVideoTrimmer) findViewById(R.id.timeLine));
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration(60);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setOnK4LVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(VideoManager.getInstance().getVideoBean().videoPath));
            mVideoTrimmer.setVideoInformationVisibility(true);
        }
    }

    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();
        VideoManager.getInstance().getVideoBean().videoPath = uri.getPath();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onError(final String message) {
        mProgressDialog.cancel();

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
