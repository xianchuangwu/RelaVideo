package video.com.relavideodemo.surface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYTextureView;
import com.thel.R;

import java.io.IOException;


public class RtmpPreviewActivity extends AppCompatActivity implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnErrorListener {


    private KSYTextureView ksyTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_preview);

    }


    public void start(View view) {
        EditText editText = findViewById(R.id.editText);
        String url = editText.getText().toString();
        if (!TextUtils.isEmpty(url)) {


            ksyTextureView = new KSYTextureView(getApplicationContext());
            try {
                ksyTextureView.setDataSource(url);
                ksyTextureView.prepareAsync();
                ksyTextureView.setOnPreparedListener(this);
                ksyTextureView.setOnErrorListener(this);
                RelativeLayout relativeLayout = findViewById(R.id.layout_surface_view);
                relativeLayout.addView(ksyTextureView);
            } catch (IOException e) {
                e.printStackTrace();
            }

            view.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(this, "input rtmp url", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        ksyTextureView.start();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        Toast.makeText(this, "error code:" + i, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ksyTextureView.release();
        ksyTextureView = null;
    }
}
