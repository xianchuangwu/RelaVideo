package video.com.relavideolibrary.thread;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import baidu.encode.ExtractDecodeEditEncodeMux;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.BaseApplication;
import video.com.relavideolibrary.Utils.FFmpegUtils;
import video.com.relavideolibrary.camera.utils.DateUtils;
import video.com.relavideolibrary.manager.VideoManager;

/**
 * Created by chad
 * Time 17/12/21
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class EditVideoThread extends Thread implements ExtractDecodeEditEncodeMux.ResultListener {

    public static final String TAG = "EditVideoThread";

    private final String mVideoPath;
    private final int filterId;

    public EditVideoThread(EditVideoListener editVideoListener) {
        this.editVideoListener = editVideoListener;
        mVideoPath = VideoManager.getInstance().getVideoBean().videoPath;
        filterId = VideoManager.getInstance().getVideoBean().filterId;
    }

    @Override
    public void run() {
        super.run();

        if (filterId == -1) {
            addBGM(mVideoPath);
        } else {
            generateFilter();
        }
    }

    private void generateFilter() {
        ExtractDecodeEditEncodeMux test = new ExtractDecodeEditEncodeMux(BaseApplication.context);
        if (filterId != -1) {
            test.setFilterType(generateGPUImageFilter(filterId));
        }
        try {
            test.testExtractDecodeEditEncodeMuxAudioVideo(mVideoPath, this);

        } catch (Throwable tr) {
            if (editVideoListener != null)
                editVideoListener.onEditVideoError("generate filter failed");

        }
    }

    private void addBGM(String path) {
        if (TextUtils.isEmpty(VideoManager.getInstance().getMusicBean().url)) {
            if (editVideoListener != null) editVideoListener.onEditVideoSuccess(path);
        } else {
            FFmpeg fFmpeg = FFmpeg.getInstance(BaseApplication.context);
            try {
                fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                    @Override
                    public void onFailure() {
                        Log.e(TAG, "FFmpeg is not supported on your device");
                    }
                });
            } catch (FFmpegNotSupportedException e) {
                Log.e(TAG, "FFmpeg is not supported on your device");
            }
            extractorVideo(path);
        }
    }

    private void composeVideo() {
        FFmpeg fFmpeg = getFfmepgObj();
        final String composePath = setFile("compose_video.mp4");
        final String videoTrackPath = setFile("video_track.mp4");
        final String mergeAudioPath = setFile("compose_audio.aac");

        try {
            FFmpegUtils.getInstance().composeVideo(fFmpeg, videoTrackPath, mergeAudioPath, composePath, 9000, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    if (editVideoListener != null)
                        editVideoListener.onEditVideoSuccess(composePath);
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
//                    printMsg("第" + position + "段视频添加背景乐 progress :" + s);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private void composeAudio() {
        FFmpeg fFmpeg = getFfmepgObj();
        final String bgmVolPath = setFile("bgm_vol.aac");
        final String volPath = setFile("audio_track_vol.aac");
        final String mergeAudioPath = setFile("compose_audio.aac");
        try {
            FFmpegUtils.getInstance().composeAudio(fFmpeg, volPath, bgmVolPath, mergeAudioPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    composeVideo();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
//                    printMsg("混合音频 onStart position :" + position + ",progress :" + s);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private void setBGMAudioVol() {
        FFmpeg fFmpeg = getFfmepgObj();
        final String splitPath = setFile("bgm_split.aac");
        final String bgmVolPath = setFile("bgm_vol.aac");
        float volume = VideoManager.getInstance().getMusicVolumn();
        printMsg("setBGMAudioVol volume:" + volume);
        try {
            FFmpegUtils.getInstance().setAACVol(fFmpeg, splitPath, volume, bgmVolPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    composeAudio();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
//                    printMsg("修改背景乐音量 position :" + position + ",progress :" + s);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private void splitMusic() {
        FFmpeg fFmpeg = getFfmepgObj();
        final String splitPath = setFile("bgm_split.aac");

        long starts = VideoManager.getInstance().getMusicBean().startTime;
        long ends = VideoManager.getInstance().getMusicBean().endTime;

        String start = DateUtils.timeFormat(starts);
        String end = DateUtils.timeFormat(ends);
        try {
            FFmpegUtils.getInstance().splitMusic(fFmpeg, VideoManager.getInstance().getMusicBean().url, start, end, splitPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    setBGMAudioVol();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
//                    printMsg("截取背景乐 position :" + position + ",progress :" + s);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private void setAudioVol() {
        FFmpeg fFmpeg = getFfmepgObj();
        final String audioTrackPath = setFile("audio_track.aac");
        final String volPath = setFile("audio_track_vol.aac");
        float volume = VideoManager.getInstance().getVideoVolumn();
        printMsg("setAudioVol volume:" + volume);
        try {
            FFmpegUtils.getInstance().setAACVol(fFmpeg, audioTrackPath, volume, volPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    splitMusic();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private void extractorAudio(String videoPath) {
        FFmpeg fFmpeg = getFfmepgObj();
        final String audioTrackPath = setFile("audio_track.aac");
        try {
            FFmpegUtils.getInstance().extractorAudio(fFmpeg, videoPath, audioTrackPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    setAudioVol();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            printMsg("extractorAudio exception :" + Log.getStackTraceString(e));
            interrupt();
        }
    }

    private void extractorVideo(final String videoPath) {
        FFmpeg fFmpeg = getFfmepgObj();
        final String videoTrackPath = setFile("video_track.mp4");
        try {
            FFmpegUtils.getInstance().extractorVideo(fFmpeg, videoPath, videoTrackPath, new FFmpegUtils.FFmpegResponseListener() {
                @Override
                public void onSuccess(String s) {
                    super.onSuccess(s);
                    extractorAudio(videoPath);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
            printMsg("extractorVideo exception :" + Log.getStackTraceString(e));
            interrupt();
        }
    }

    /**
     * ffmpeg同一时间不能执行多个命令，执行多个命令需要通过多线程管理,
     * 由于使用的ffmpeg编译库内部执行命令都是异步，所以只需返回不同的ffmpeg对象
     *
     * @return
     */
    private synchronized FFmpeg getFfmepgObj() {
        FFmpeg fFmpeg = FFmpeg.getInstance(BaseApplication.context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "FFmpeg is not supported on your device");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.e(TAG, "FFmpeg is not supported on your device");
        }
        return fFmpeg;
    }

    private void printMsg(String msg) {
        Log.d(TAG, msg);
    }

    private String setFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/rela");
        sb.append("/rela");
        sb.append('-');
        sb.append(fileName);
        return sb.toString();
    }

    private GPUImageFilter generateGPUImageFilter(int resId) {

        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
        lookupFilter.setBitmap(BitmapFactory.decodeResource(BaseApplication.context.getResources(), resId));
        return lookupFilter;
    }

    private EditVideoListener editVideoListener;

    @Override
    public void onFilterGenerateResult(String extra) {
        addBGM(extra);
    }

    public interface EditVideoListener {
        void onEditVideoSuccess(String path);

        void onEditVideoError(String message);
    }
}
