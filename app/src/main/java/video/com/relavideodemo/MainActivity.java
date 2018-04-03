package video.com.relavideodemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;
import video.com.relavideolibrary.interfaces.MusicListSyncDataCallback;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.model.MusicCategoryBean;
import video.com.relavideolibrary.surface.RecordingActivity;
import video.com.relavideolibrary.view.RelaBigGiftView;

public class MainActivity extends AppCompatActivity implements FilterDataCallback, MusicCategoryCallback, MusicListCallback, View.OnClickListener {

    private TextView textView;
    private String path;
    private RelaBigGiftView bigGiftView;
    private ImageView filter_image;

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
        filter_image = findViewById(R.id.filter_image);
        filter_image.setOnClickListener(this);

        RelaVideoSDK.init(getApplicationContext());
        new RelaVideoSDK()
                .addFilter(this)
                .addMusicCategory(this)
                .addMusicList(this);

        if (bigGiftView == null) {
            FrameLayout container = findViewById(R.id.container);
            bigGiftView = new RelaBigGiftView(this, "http://live-yf-hdl.huomaotv.cn/live/bcfpxN35275.flv?from=huomaoroom");
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
            container.addView(bigGiftView, layoutParams);
        } else {
            bigGiftView.reload("http://pro.thel.co/gift/video/1514199928405jendak.mp4");
        }

//        handler.sendEmptyMessageDelayed(0, 2000);
    }

//    private Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            boolean b = isAppOnForeground();
//            Toast.makeText(MainActivity.this, "isAppOnForeground: " + b, Toast.LENGTH_SHORT).show();
//            handler.sendEmptyMessageDelayed(0, 2000);
//            return false;
//        }
//    });


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_image:
                GPUImage gpuImage = new GPUImage(this);
                gpuImage.setImage(BitmapFactory.decodeResource(getResources(), R.mipmap.app_lockscreen_bg));

                GPUImageLookupFilter filter = new GPUImageLookupFilter();
                int[] filterResId = {R.raw.a1_chenguang
                        , R.raw.a2_haiyang
                        , R.raw.a3_qingcao
                        , R.raw.b1_heibai
                        , R.raw.b2_mingliang
                        , R.raw.b3_chenmo
                        , R.raw.f1_weifeng
                        , R.raw.f2_lieri
                        , R.raw.f3_nihong
                        , R.raw.f4_anyong
                        , R.raw.f5_huanghun
                        , R.raw.f6_shiguang
                        , R.raw.f7_qingcao
                        , R.raw.y1_zaocan
                        , R.raw.y2_yanmai
                        , R.raw.s1_xuanlan
                        , R.raw.s2_yexing
                };
                filter.setBitmap(BitmapFactory.decodeResource(getResources(), filterResId[(int) (Math.random() * filterResId.length)]));
                gpuImage.setFilter(filter);

                filter_image.setImageBitmap(gpuImage.getBitmapWithFilterApplied());
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bigGiftView != null) bigGiftView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bigGiftView != null) bigGiftView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    startActivityForResult(new Intent(MainActivity.this, RecordingActivity.class), Constant.IntentCode.REQUEST_CODE_RECORDING);
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
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
        if (requestCode == Constant.IntentCode.REQUEST_CODE_RECORDING && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            path = extras.getString(Constant.BundleConstants.RESULT_VIDEO_PATH);
            if (!TextUtils.isEmpty(path))
                textView.setText(path);
        }
    }


    @Override
    public List<FilterBean> getFilterData() {
        int[] filterResId = {-1
                , R.raw.a1_chenguang
                , R.raw.a2_haiyang
                , R.raw.a3_qingcao
                , R.raw.b1_heibai
                , R.raw.b2_mingliang
                , R.raw.b3_chenmo
                , R.raw.f1_weifeng
                , R.raw.f2_lieri
                , R.raw.f3_nihong
                , R.raw.f4_anyong
                , R.raw.f5_huanghun
                , R.raw.f6_shiguang
                , R.raw.f7_qingcao
                , R.raw.y1_zaocan
                , R.raw.y2_yanmai
                , R.raw.s1_xuanlan
                , R.raw.s2_yexing
        };

        String[] filterName = {
                getString(video.com.relavideolibrary.R.string.original_film)
                , "A1", "A2", "A3"
                , "B1", "B2", "B3"
                , "F1", "F2", "F3", "F4", "F5", "F6", "F7"
                , "Y1", "Y2"
                , "S1", "S2"
        };

        ArrayList<FilterBean> list = new ArrayList<>();

        for (int i = 0; i < filterResId.length; i++) {
            FilterBean filterBean = new FilterBean();
            filterBean.filterName = filterName[i];
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
    public void getMusicList(int category, final MusicListSyncDataCallback musicListSyncDataCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String json = "{\"result\":\"1\",\"data\":[{\"musicId\":37,\"name\":\"Higekitekina\",\"singer\":\"Fabian Measures\",\"musicHours\":216,\"url\":\"http://music.fffffive.com/1500459192438.mp3\",\"categoryName\":\"忧郁\",\"collectStatus\":0},{\"musicId\":36,\"name\":\"Tomorrow We Continue Solo Piano\",\"singer\":\"Artofescapism\",\"musicHours\":56,\"url\":\"http://music.fffffive.com/1500459184115.mp3\",\"categoryName\":\"忧郁\",\"collectStatus\":0},{\"musicId\":35,\"name\":\"By The Coast 2004\"" +
                        ",\"singer\":\"Antony Raijekov\",\"musicHours\":182,\"url\":\"http://music.fffffive.com/1500459175352.mp3\",\"categoryName\":\"忧郁\",\"collectStatus\":0},{\"musicId\":34,\"name\":\"The Waning Moon\",\"singer\":\"Anima\",\"musicHours\":363,\"url\":\"http://music.fffffive.com/1500459155032.mp3\",\"categoryName\":\"忧郁\",\"collectStatus\":0},{\"musicId\":33,\"name\":\"Humming\",\"singer\":\"David Szesztay\",\"musicHours\":47,\"url\":\"http://music.fffffive.com/1500459145639.mp3\"" +
                        ",\"categoryName\":\"浪漫\",\"collectStatus\":0},{\"musicId\":32,\"name\":\"Passing Time\",\"singer\":\"BoxCat Games\",\"musicHours\":89,\"url\":\"http://music.fffffive.com/1500459137518.mp3\",\"categoryName\":\"浪漫\",\"collectStatus\":0},{\"musicId\":30,\"name\":\"Qu Paciencia\",\"singer\":\"Los Sundayers\",\"musicHours\":173,\"url\":\"http://music.fffffive.com/1500459106536.mp3\",\"categoryName\":\"欢乐\",\"collectStatus\":0},{\"musicId\":29,\"name\":\"Hey\",\"singer\":\"Juanitos\"" +
                        ",\"musicHours\":141,\"url\":\"http://music.fffffive.com/1500459096748.mp3\",\"categoryName\":\"欢乐\",\"collectStatus\":0},{\"musicId\":27,\"name\":\"Wholesome 7\",\"singer\":\"Dave Depper\",\"musicHours\":39,\"url\":\"http://music.fffffive.com/1500459076958.mp3\",\"categoryName\":\"欢乐\",\"collectStatus\":0},{\"musicId\":26,\"name\":\"Hey Hey Hey Happy Birthday\",\"singer\":\"Daniel C Smith\",\"musicHours\":85,\"url\":\"http://music.fffffive.com/1500459067117.mp3\"" +
                        ",\"categoryName\":\"欢乐\",\"collectStatus\":0},{\"musicId\":25,\"name\":\"Revved Up\",\"singer\":\"Adam Selzer\",\"musicHours\":29,\"url\":\"http://music.fffffive.com/1500459048278.mp3\",\"categoryName\":\"欢乐\",\"collectStatus\":0},{\"musicId\":24,\"name\":\"Hola Hola Bossa Nova\",\"singer\":\"Juanitos\",\"musicHours\":207,\"url\":\"http://music.fffffive.com/1500459038102.mp3\",\"categoryName\":\"怀旧\",\"collectStatus\":0},{\"musicId\":23,\"name\":\"La Couleur Et Lair\"" +
                        ",\"singer\":\"Byzance Nord\",\"musicHours\":214,\"url\":\"http://music.fffffive.com/1500459028189.mp3\",\"categoryName\":\"怀旧\",\"collectStatus\":0},{\"musicId\":22,\"name\":\"Catarment\",\"singer\":\"Byzance Nord\",\"musicHours\":315,\"url\":\"http://music.fffffive.com/1500459006429.mp3\",\"categoryName\":\"怀旧\",\"collectStatus\":0},{\"musicId\":21,\"name\":\"Bleuacide\",\"singer\":\"Graphiqs Groove\",\"musicHours\":313,\"url\":\"http://music.fffffive.com/1500458989947.mp3\"" +
                        ",\"categoryName\":\"动感\",\"collectStatus\":0},{\"musicId\":20,\"name\":\"Smudj\",\"singer\":\"Foniqz \",\"musicHours\":315,\"url\":\"http://music.fffffive.com/1500458978086.mp3\",\"categoryName\":\"动感\",\"collectStatus\":0},{\"musicId\":19,\"name\":\"Storm\",\"singer\":\"BoxCat Games\",\"musicHours\":82,\"url\":\"http://music.fffffive.com/1500458967526.mp3\",\"categoryName\":\"动感\",\"collectStatus\":0},{\"musicId\":18,\"name\":\"Hot Salsa Trip\",\"singer\":\"Arsonist\"" +
                        ",\"musicHours\":262,\"url\":\"http://music.fffffive.com/1500458950020.mp3\",\"categoryName\":\"动感\",\"collectStatus\":0},{\"musicId\":17,\"name\":\"Type Your Name Here (Short)\",\"singer\":\"No Name\",\"musicHours\":290,\"url\":\"http://music.fffffive.com/1500458936824.mp3\",\"categoryName\":\"动感\",\"collectStatus\":0},{\"musicId\":16,\"name\":\"Parasite\",\"singer\":\"Lamprey\",\"musicHours\":214,\"url\":\"http://music.fffffive.com/1500458899890.mp3\",\"categoryName\":\"动感\",\"collectStatus\":0}],\"errcode\":\"\",\"errdesc\":\"\"}";
                MusicListBean musicListBean = new Gson().fromJson(json, MusicListBean.class);
                if (musicListSyncDataCallback != null)
                    musicListSyncDataCallback.onSuccess(musicListBean.data);
            }
        }).start();
    }

    public void test(View view) {
        bigGiftView.reload("http://pro.thel.co/gift/video/1514199928405jendak.mp4");
    }

    public void playVideo(View view) {
        startActivity(new Intent(this, VideoListActivity.class));
    }

    public boolean isAppOnForeground() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            String packageName = getPackageName();
            if (appProcesses == null) {
                return false;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }
}
