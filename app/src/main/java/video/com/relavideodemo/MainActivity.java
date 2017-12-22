package video.com.relavideodemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.model.MusicBean;
import video.com.relavideolibrary.model.MusicCategoryBean;
import video.com.relavideolibrary.surface.RecordingActivity;

public class MainActivity extends AppCompatActivity implements FilterDataCallback, MusicCategoryCallback, MusicListCallback {

    private TextView textView;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //        andorid 7.0 FileUriExposedException
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.path);

        RelaVideoSDK.initialization(this, this, this);
    }

    public void recoder(View v) {

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.CAMERA
                , Manifest.permission.RECORD_AUDIO
                , Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    startActivity(new Intent(MainActivity.this, RecordingActivity.class));
                }
            }
        });

    }

    public void preview(View view) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "empty path!", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "file no find!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IntentCode.REQUEST_CODE_RECODER && resultCode == Constant.IntentCode.RESULT_CODE_RECODER) {
            path = data.getStringExtra(Constant.RECODER_RESULT_PATH);
            if (!TextUtils.isEmpty(path))
                textView.setText(path);
        }
    }


    @Override
    public List<FilterBean> getFilterData() {
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

    @Override
    public List<MusicCategoryBean> getMusicCategoryData() {
        List<MusicCategoryBean> categoryBeanList = new ArrayList<>();

        int[] categoryCodeArr = {-2, -1, 0, 6, 5, 4, 3, 2, 1};
        String[] categoryNameArr = getResources().getStringArray(R.array.music_category);
        int[] selectImageId = {R.mipmap.ic_new_selected
                , R.mipmap.ic_hot_selected, R.mipmap.ic_love_selected
                , R.mipmap.ic_quiet_selected, R.mipmap.ic_classical_selected
                , R.mipmap.ic_sad_selected, R.mipmap.ic_exciting_selected
                , R.mipmap.ic_happy_selected, R.mipmap.ic_movie_selected};
        int[] unSelectImageId = {R.mipmap.ic_new
                , R.mipmap.ic_hot, R.mipmap.ic_love
                , R.mipmap.ic_quiet, R.mipmap.ic_classical
                , R.mipmap.ic_sad, R.mipmap.ic_exciting
                , R.mipmap.ic_happy, R.mipmap.ic_movie};
        for (int i = 0; i < categoryNameArr.length; i++) {
            MusicCategoryBean musicCategoryBean = new MusicCategoryBean();
            musicCategoryBean.categoryName = categoryNameArr[i];
            musicCategoryBean.selectImage = selectImageId[i];
            musicCategoryBean.unSelectImage = unSelectImageId[i];
            musicCategoryBean.categoryCode = categoryCodeArr[i];
            categoryBeanList.add(musicCategoryBean);
        }

        return categoryBeanList;
    }

    @Override
    public List<MusicBean> getMusicList(int category) {

        List<MusicBean> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MusicBean musicBean = new MusicBean();
            musicBean.musicId = (57 + i);
            musicBean.name = "克罗地亚狂想曲";
            musicBean.singer = "狄仁杰,王孝杰";
            musicBean.musicHours = 151;
            musicBean.url = "http://music.fffffive.com/1502085142227.mp3";
            musicBean.categoryName = "怀旧";
            musicBean.collectStatus = 0;
            list.add(musicBean);
        }
        return list;
    }
}
