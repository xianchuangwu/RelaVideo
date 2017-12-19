package video.com.relavideodemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.surface.RecordingActivity;

public class MainActivity extends AppCompatActivity implements FilterDataCallback {

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.path);

        RelaVideoSDK.initialization(this);
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
        int[] filterResId = {R.raw.filter_black_white1, R.raw.filter_black_white2, R.raw.filter_black_white3
                , R.raw.filter_bopu, R.raw.filter_yecan, R.raw.filter_qingxing, R.raw.filter_guobao
                , R.raw.filter_fenhong, R.raw.filter_baohe_low, R.raw.filter_honglv, R.raw.filter_huanglv
                , R.raw.filter_baohe_high, R.raw.filter_test};
        startActivity(new Intent(this, RecordingActivity.class));
//        startActivity(new Intent(this, EditActivity.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IntentCode.REQUEST_CODE_RECODER && resultCode == Constant.IntentCode.RESULT_CODE_RECODER) {
            String path = data.getStringExtra(Constant.RECODER_RESULT_PATH);
            if (!TextUtils.isEmpty(path))
                textView.setText(path);
        }

    }


    @Override
    public List<FilterBean> onComplete() {
        int[] filterResId = {-1, R.raw.filter_black_white1, R.raw.filter_black_white2, R.raw.filter_black_white3
                , R.raw.filter_bopu, R.raw.filter_yecan, R.raw.filter_qingxing, R.raw.filter_guobao
                , R.raw.filter_fenhong, R.raw.filter_baohe_low, R.raw.filter_honglv, R.raw.filter_huanglv
                , R.raw.filter_baohe_high, R.raw.filter_test};

        ArrayList<FilterBean> list = new ArrayList<>();

        for (int i = 0; i < filterResId.length; i++) {
            FilterBean filterBean = new FilterBean();
            if (i == 0) {
                filterBean.filterName = getString(video.com.relavideolibrary.R.string.original_film);
            } else {
                filterBean.filterName = "R" + i;
            }
            filterBean.filterId = filterResId[i];
            list.add(filterBean);
        }

        return list;
    }
}
