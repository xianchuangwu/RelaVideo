package video.com.relavideodemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;
import video.com.relavideolibrary.interfaces.MusicListSyncDataCallback;
import video.com.relavideolibrary.model.FilterBean;
import video.com.relavideolibrary.model.MusicBean;
import video.com.relavideolibrary.model.MusicCategoryBean;
import video.com.relavideolibrary.surface.RecordingActivity;
import video.com.relavideolibrary.view.RelaBigGiftView;

public class MainActivity extends AppCompatActivity implements FilterDataCallback, MusicCategoryCallback, MusicListCallback {

    private TextView textView;
    private String path;
    private RelaBigGiftView bigGiftView;

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
//        ImageView filter_image = findViewById(R.id.filter_image);
//        GPUImage gpuImage = new GPUImage(this);
//        gpuImage.setImage(BitmapFactory.decodeResource(getResources(), R.mipmap.app_lockscreen_bg));
//
//        GPUImageLookupFilter filter = new GPUImageLookupFilter();
//        filter.setBitmap(BitmapFactory.decodeResource(getResources(), R.raw.filter_black_white1));
//        gpuImage.setFilter(filter);
//
//        filter_image.setImageBitmap(gpuImage.getBitmapWithFilterApplied());

        new RelaVideoSDK()
                .init(getApplicationContext())
                .addFilter(this)
                .addMusicCategory(this)
                .addMusicList(this);


//        ListeningExecutorService executorService = MoreExecutors.newDirectExecutorService();
//        final ListenableFuture<Integer> listenableFuture = executorService.submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                System.out.println("call execute..");
//                TimeUnit.SECONDS.sleep(1);
//                return 7;
//            }
//        });
//
//        Futures.addCallback(listenableFuture, new FutureCallback<Integer>() {
//            @Override
//            public void onSuccess(@Nullable Integer result) {
//
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//
//            }
//        });

        if (bigGiftView == null) {
            FrameLayout container = findViewById(R.id.container);
            bigGiftView = new RelaBigGiftView(this, "http://live-yf-hdl.huomaotv.cn/live/bcfpxN35275.flv?from=huomaoroom");
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
//            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            container.addView(bigGiftView, layoutParams);
        } else {
            bigGiftView.reload("http://pro.thel.co/gift/video/1514199928405jendak.mp4");
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
    public void getMusicList(int category, final MusicListSyncDataCallback musicListSyncDataCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
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

                if (musicListSyncDataCallback != null) musicListSyncDataCallback.onSuccess(list);
            }
        }).start();
    }

    public void test(View view) {
        bigGiftView.reload("http://pro.thel.co/gift/video/1514199928405jendak.mp4");
    }

    public static void trackMuxer(String videoPath, String audioPath, String outPath) {
        MediaExtractor videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat videoFormat = null;
        int videoTrackIndex = -1;
        int videoTrackCount = videoExtractor.getTrackCount();
        for (int i = 0; i < videoTrackCount; i++) {
            videoFormat = videoExtractor.getTrackFormat(i);
            String mime = videoFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                videoTrackIndex = i;
                break;
            }
        }

        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(audioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat audioFormat = null;
        int audioTrackIndex = -1;
        int audioTrackCount = audioExtractor.getTrackCount();
        for (int i = 0; i < audioTrackCount; i++) {
            audioFormat = audioExtractor.getTrackFormat(i);
            String mime = audioFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i;
                break;
            }
        }

        videoExtractor.selectTrack(videoTrackIndex);
        audioExtractor.selectTrack(audioTrackIndex);

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

        MediaMuxer mediaMuxer = null;
        try {
            mediaMuxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int writeVideoTrackIndex = mediaMuxer.addTrack(videoFormat);
        int writeAudioTrackIndex = mediaMuxer.addTrack(audioFormat);
        mediaMuxer.start();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1000);
        long videoSampleTime = getSampleTime(videoExtractor, byteBuffer);
        while (true) {
            int readVideoSampleSize = videoExtractor.readSampleData(byteBuffer, 0);
            if (readVideoSampleSize < 0) {
                break;
            }
            videoBufferInfo.size = readVideoSampleSize;
            videoBufferInfo.presentationTimeUs += videoSampleTime;
            videoBufferInfo.offset = 0;
            videoBufferInfo.flags = videoExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeVideoTrackIndex, byteBuffer, videoBufferInfo);
            videoExtractor.advance();
        }
        long audioSampleTime = getSampleTime(audioExtractor, byteBuffer);
        while (true) {
            int readAudioSampleSize = audioExtractor.readSampleData(byteBuffer, 0);
            if (readAudioSampleSize < 0) {
                break;
            }

            audioBufferInfo.size = readAudioSampleSize;
            audioBufferInfo.presentationTimeUs += audioSampleTime;
            audioBufferInfo.offset = 0;
            audioBufferInfo.flags = audioExtractor.getSampleFlags();
            mediaMuxer.writeSampleData(writeAudioTrackIndex, byteBuffer, audioBufferInfo);
            audioExtractor.advance();
        }

        mediaMuxer.stop();
        mediaMuxer.release();
        videoExtractor.release();
        audioExtractor.release();
    }

    private static long getSampleTime(MediaExtractor mediaExtractor, ByteBuffer buffer) {
        long videoSampleTime;
//            mediaExtractor.readSampleData(buffer, 0);
//            //skip first I frame
//            if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC)
//                mediaExtractor.advance();
        mediaExtractor.readSampleData(buffer, 0);
        long firstVideoPTS = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(buffer, 0);
        long SecondVideoPTS = mediaExtractor.getSampleTime();
        videoSampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
        Log.d("MediaMuxerUtil", "videoSampleTime is " + videoSampleTime);
        return videoSampleTime;
    }

    public static boolean muxAacMp4(String aacPath, String mp4Path, String outPath) {
        try {
            AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(aacPath));
            Movie videoMovie = MovieCreator.build(mp4Path);
            Track videoTracks = null;// 获取视频的单纯视频部分
            for (Track videoMovieTrack : videoMovie.getTracks()) {
                if ("vide".equals(videoMovieTrack.getHandler())) {
                    videoTracks = videoMovieTrack;
                }
            }

            Movie resultMovie = new Movie();
            resultMovie.addTrack(videoTracks);// 视频部分
            resultMovie.addTrack(aacTrack);// 音频部分

            Container out = new DefaultMp4Builder().build(resultMovie);
            FileOutputStream fos = new FileOutputStream(new File(outPath));
            out.writeContainer(fos.getChannel());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
