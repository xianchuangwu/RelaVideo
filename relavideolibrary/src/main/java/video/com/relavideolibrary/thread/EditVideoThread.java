package video.com.relavideolibrary.thread;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimingLogger;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import baidu.encode.ExtractDecodeEditEncodeMux;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.FFmpegUtils;
import video.com.relavideolibrary.Utils.FileManager;
import video.com.relavideolibrary.camera.utils.DateUtils;
import video.com.relavideolibrary.manager.VideoManager;

/**
 * Created by chad
 * Time 17/12/21
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 合成滤镜-->合成背景乐
 */

public class EditVideoThread extends Thread implements ExtractDecodeEditEncodeMux.ResultListener {

    public static final String TAG = "EditVideoThread";

    private final String mVideoPath;
    private final int filterId;
    //adb shell setprop log.tag.EditVideoThread VERBOSE
    private final TimingLogger timingLogger;

    public EditVideoThread(EditVideoListener editVideoListener) {
        this.editVideoListener = editVideoListener;
        mVideoPath = VideoManager.getInstance().getVideoBean().videoPath;
        filterId = VideoManager.getInstance().getVideoBean().filterId;
        timingLogger = new TimingLogger(TAG, "edit video");
    }

    @Override
    public void run() {
        super.run();

        if (filterId == -1) {
            printMsg("未添加滤镜,直接合成背景乐");
            addBGM(mVideoPath);
        } else {
            printMsg("开始合成滤镜");
            generateFilter();
        }
    }

    private void generateFilter() {
        ExtractDecodeEditEncodeMux test = new ExtractDecodeEditEncodeMux(RelaVideoSDK.context);
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
            printMsg("未添加背景乐");
            timingLogger.dumpToLog();
            if (editVideoListener != null) editVideoListener.onEditVideoSuccess(path);
        } else {
            printMsg("开始合成背景乐");
            FFmpeg fFmpeg = FFmpeg.getInstance(RelaVideoSDK.context);
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
                    timingLogger.addSplit("音视频合成");
                    timingLogger.dumpToLog();
                    if (editVideoListener != null)
                        editVideoListener.onEditVideoSuccess(composePath);
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("音视频合成 onProgress :" + s);
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
                    timingLogger.addSplit("合成音轨");
                    composeVideo();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("合成音轨 onProgress :" + s);
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
                    timingLogger.addSplit("设置背景乐音量");
                    composeAudio();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("设置背景乐音量 onProgress :" + s);
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
                    timingLogger.addSplit("剪裁背景乐");
                    setBGMAudioVol();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("剪裁背景乐 onProgress :" + s);
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
                    timingLogger.addSplit("设置视频音量");
                    splitMusic();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("设置视频音量 onProgress :" + s);
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
                    timingLogger.addSplit("提取音轨");
                    setAudioVol();
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("提取音轨 onProgress :" + s);
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
                    timingLogger.addSplit("提取视轨");
                    extractorAudio(videoPath);
                }

                @Override
                public void onProgress(String s) {
                    super.onProgress(s);
                    printMsg("提取视轨 onProgress :" + s);
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
        FFmpeg fFmpeg = FFmpeg.getInstance(RelaVideoSDK.context);
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
        return FileManager.getOtherFile(fileName);
    }

    private GPUImageFilter generateGPUImageFilter(int resId) {

        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
        lookupFilter.setBitmap(BitmapFactory.decodeResource(RelaVideoSDK.context.getResources(), resId));
        return lookupFilter;
    }

    private EditVideoListener editVideoListener;

    @Override
    public void onFilterGenerateResult(String extra) {
        timingLogger.addSplit("合成滤镜");
        addBGM(extra);
    }

    public interface EditVideoListener {
        void onEditVideoSuccess(String path);

        void onEditVideoError(String message);
    }
}
