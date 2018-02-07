package video.com.relavideolibrary.Utils.codec;

import android.annotation.TargetApi;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import video.com.relavideolibrary.Utils.Constant;

/**
 * Created by chad
 * Time 18/1/30
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 音频剪裁
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class AudioClip {

    private static final String TAG = "AudioClip";
    private String inputPath;
    private String outputPath;
    private long start;
    private long end;
    private boolean videoHasAudio;//是否无声视频
    private int videoSampleRate;//根据视频的音频采样率统一背景乐采样率

    public AudioClip(String inputPath, String outputPath, long start, long end) {
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.start = start;
        this.end = end;
    }

    /**
     * 作为视频背景乐剪裁时，要和视频的音频采样率统一
     *
     * @param videoHasAudio
     * @param videoSampleRate
     */
    public void setVideoSampleRate(boolean videoHasAudio, int videoSampleRate) {
        this.videoHasAudio = videoHasAudio;
        this.videoSampleRate = videoSampleRate;
    }

    public void clipMusic() {
        //适当的调整SAMPLE_SIZE可以更加精确的裁剪音乐
        final int SAMPLE_SIZE = 1024 * 512;
        MediaExtractor extractor = null;
        BufferedOutputStream outputStream = null;
        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(inputPath);
            int track = -1;
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {
                    track = i;
                    break;
                }
            }
            if (track < 0) {
                Log.d(TAG, "clipMusic track -1");
                return;
            }
            //选择音频轨道
            extractor.selectTrack(track);
            outputStream = new BufferedOutputStream(
                    new FileOutputStream(outputPath), SAMPLE_SIZE);
            start = start * 1000;
            if (videoHasAudio) {
                end = (long) ((double) end * 1000 * videoSampleRate / Constant.EncodeConfig.OUTPUT_AUDIO_SAMPLE_RATE_HZ);
            } else {
                end = end * 1000;
            }
            //跳至开始裁剪位置
            extractor.seekTo(start, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(SAMPLE_SIZE);
                int sampleSize = extractor.readSampleData(buffer, 0);
                long timeStamp = extractor.getSampleTime();
                if (timeStamp > end) {
                    break;
                }
                if (sampleSize <= 0) {
                    break;
                }
                byte[] buf = new byte[sampleSize];
                buffer.get(buf, 0, sampleSize);
                //写入文件
                outputStream.write(buf);
                //音轨数据往前读
                extractor.advance();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (extractor != null) {
                extractor.release();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
