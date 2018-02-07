package video.com.relavideolibrary.Utils;

import android.util.Log;


/**
 * Created by chad
 * Time 17/8/30
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class FFmpegUtils {

    public static final String TAG = "FFmpegUtils";

    private static FFmpegUtils fmpegUtils;

    private FFmpegUtils() {
    }

    public static FFmpegUtils getInstance() {
        if (fmpegUtils == null) {
            fmpegUtils = new FFmpegUtils();
        }
        return fmpegUtils;
    }

    /**
     * ffmpeg同一时间不能执行多个命令，执行多个命令需要通过多线程管理,
     * 由于使用的ffmpeg编译库内部执行命令都是异步，所以只需返回不同的ffmpeg对象
     *
     * @return
     */
//    private synchronized FFmpeg getFfmepgObj() {
//        FFmpeg fFmpeg = FFmpeg.getInstance(BaseApplication.context);
//        try {
//            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
//                @Override
//                public void onFailure() {
//                    Log.e(TAG, "FFmpeg is not supported on your device");
//                }
//            });
//        } catch (FFmpegNotSupportedException e) {
//            Log.e(TAG, "FFmpeg is not supported on your device");
//        }
//        return fFmpeg;
//    }

    /**
     * 从视频中提取音轨
     * -i 输入文件
     * -acodec 使用codec编解码
     * copy 拷贝原始编解码数据
     * -vn 不做视频记录(只提取音频)
     * -y 直接覆盖（如果目录下相同文件名字）
     */
//    public void extractorAudio(FFmpeg fFmpeg, String videoUrl, String outUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[7];
//        commands[0] = "-i";
//        commands[1] = videoUrl;
//        commands[2] = "-acodec";
//        commands[3] = "copy";
//        commands[4] = "-vn";
//        commands[5] = "-y";
//        commands[6] = outUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * 从视频中提取视轨
     */
//    public void extractorVideo(FFmpeg fFmpeg, String videoUrl, String outUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[7];
//        commands[0] = "-i";
//        commands[1] = videoUrl;
//        commands[2] = "-vcodec";
//        commands[3] = "copy";
//        commands[4] = "-an";
//        commands[5] = "-y";
//        commands[6] = outUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * 截取音乐
     *
     * @param musicUrl
     * @param start    字符串格式00:00:00
     * @param second   字符串格式00:00:00
     * @param outUrl
     */
//    public void splitMusic(FFmpeg fFmpeg, String musicUrl, String start, String second, String outUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[10];
//        commands[0] = "-i";
//        commands[1] = musicUrl;
//        commands[2] = "-ss";
//        commands[3] = start;
////        commands[4] = "-t";//持续时间
//        commands[4] = "-to";//结束时间
//        commands[5] = second;
//        commands[6] = "-acodec";
//        commands[7] = "copy";
//        commands[8] = "-y";
//        commands[9] = outUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * @param aacUrl
     * @param vol                    0-1, 原声的百分比数值
     * @param outUrl
     * @param fFmpegResponseListener
     * @throws FFmpegCommandAlreadyRunningException
     */
//    public void setAACVol(FFmpeg fFmpeg, String aacUrl, float vol, String outUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[6];
//        commands[0] = "-i";
//        commands[1] = aacUrl;
//        commands[2] = "-af";
//        commands[3] = "volume=" + vol;
//        commands[4] = "-y";
//        commands[5] = outUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * 混合两个音频文件
     *
     * @param audio1                 最后的合成的音频时长以第一个音频的时长为准。
     * @param audio2
     * @param outputUrl
     * @param fFmpegResponseListener
     * @throws FFmpegCommandAlreadyRunningException
     */
//    public void composeAudio(FFmpeg fFmpeg, String audio1, String audio2, String outputUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[14];
//        commands[0] = "-i";
//        commands[1] = audio1;
//        commands[2] = "-i";
//        commands[3] = audio2;
//        commands[4] = "-filter_complex";
//        commands[5] = "[0:a][1:a]amerge=inputs=2[aout]";
//        commands[6] = "-map";
//        commands[7] = "[aout]";
//        commands[8] = "-ac";
//        commands[9] = "2";
//        commands[10] = "-c:a";
//        commands[11] = "aac";
//        commands[12] = "-y";
//        commands[13] = outputUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//
//    }

    /**
     * 音频文件合成到视频
     *
     * @param videoUrl
     * @param musicOrAudio
     * @param outputUrl
     * @param second
     * @param fFmpegResponseListener
     * @throws FFmpegCommandAlreadyRunningException
     */
//    public void composeVideo(FFmpeg fFmpeg, String videoUrl, String musicOrAudio, String outputUrl, long second, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[16];
//        commands[0] = "-i";
//        commands[1] = videoUrl;
//        commands[2] = "-i";
//        commands[3] = musicOrAudio;
//        commands[4] = "-ss";
//        commands[5] = "00:00:00";
//        commands[6] = "-t";
//        commands[7] = String.valueOf(second);
//        commands[8] = "-vcodec";
//        commands[9] = "copy";
//        commands[10] = "-acodec";
//        commands[11] = "copy";
//        commands[12] = "-y";
//        commands[13] = "-bsf:a";
//        commands[14] = "aac_adtstoasc";
//        commands[15] = outputUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }
//
//    public void composeVideo(FFmpeg fFmpeg, String videoUrl, String musicOrAudio, String outputUrl, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[10];
//        commands[0] = "-i";
//        commands[1] = videoUrl;
//        commands[2] = "-i";
//        commands[3] = musicOrAudio;
//        commands[4] = "-c:v";
//        commands[5] = "copy";
//        commands[6] = "-c:a";
//        commands[7] = "copy";
//        commands[8] = "-y";
//        commands[9] = outputUrl;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * 拼接多段视频 这些视频的profile必须一致
     *
     * @param fileListPath
     * @param outPutPath
     * @param fFmpegResponseListener
     * @throws FFmpegCommandAlreadyRunningException
     */
//    public void mergeVideo(FFmpeg fFmpeg, String fileListPath, String outPutPath, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[10];
//        commands[0] = "-f";
//        commands[1] = "concat";
//        commands[2] = "-safe";
//        commands[3] = "0";
//        commands[4] = "-i";
//        commands[5] = fileListPath;
//        commands[6] = "-c";
//        commands[7] = "copy";
//        commands[8] = "-y";
//        commands[9] = outPutPath;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }

    /**
     * 视频剪裁
     *
     * @param fFmpeg
     * @param startTime              00:00:00
     * @param endTime                00:00:00
     * @param src
     * @param output
     * @param fFmpegResponseListener
     * @throws FFmpegCommandAlreadyRunningException
     */
//    public void splitVideo(FFmpeg fFmpeg, String startTime, String endTime, String src, String output, FFmpegResponseListener fFmpegResponseListener) throws FFmpegCommandAlreadyRunningException {
//        String[] commands = new String[9];
//        commands[0] = "-ss";
//        commands[1] = startTime;
//        commands[2] = "-i";
//        commands[3] = src;
//        commands[4] = "-c";
//        commands[5] = "copy";
//        commands[6] = "-t";
//        commands[7] = endTime;
//        commands[8] = output;
//        fFmpeg.execute(commands, fFmpegResponseListener);
//    }
//
//    public static class FFmpegResponseListener extends ExecuteBinaryResponseHandler {
//        @Override
//        public void onFailure(String s) {
////            Log.d(TAG, "Fail command : ffmpeg " + cmd);
//            Log.e(TAG, "FAILED with output : " + s);
//        }
//
//        @Override
//        public void onSuccess(String s) {
////            Log.d(TAG, "Success command : ffmpeg " + cmd);
//            Log.d(TAG, "SUCCESS with output : " + s);
//        }
//
//        @Override
//        public void onProgress(String s) {
////            Log.d(TAG, "Started command : ffmpeg " + cmd + "\nprogress : " + s);
//            Log.d(TAG, "progress : " + s);
//        }
//
//        @Override
//        public void onStart() {
//
////            Log.d(TAG, "Started command : ffmpeg " + cmd);
//            Log.d(TAG, "Started");
//        }
//
//        @Override
//        public void onFinish() {
////            Log.d(TAG, "Finished command : ffmpeg " + cmd);
//            Log.d(TAG, "Finished");
//        }
//    }
}
