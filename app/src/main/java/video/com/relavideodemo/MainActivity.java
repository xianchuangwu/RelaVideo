package video.com.relavideodemo;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;
import video.com.relavideolibrary.Utils.CameraManager;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.surface.VideoRecoderActivity;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.path);
    }

    public void recoder(View v) {

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA
                , Manifest.permission.RECORD_AUDIO
                , Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) doBackup();
            }
        });

    }

    private void doBackup() {
        CameraManager.CameraParams cameraParams = new CameraManager.CameraParams();
        cameraParams.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        VideoRecoderActivity.startActivity(this, CameraManager.CameraParams.toJson(cameraParams));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_START_RECODER && resultCode == Constant.RESULT_START_RECODER) {
            String path = data.getStringExtra(Constant.RECODER_RESULT_PATH);
            if (!TextUtils.isEmpty(path))
                textView.setText(path);
        }

    }


}
