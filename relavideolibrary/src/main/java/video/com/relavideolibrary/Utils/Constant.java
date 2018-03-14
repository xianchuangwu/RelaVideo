package video.com.relavideolibrary.Utils;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

/**
 * Created by chad
 * Time 17/12/4
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class Constant {

    public static final String KEY_INTENT_CAMERA_PARAMS = "camera params";

    public static final String VIDEO_BEAN = "video bean";

    public static final int HOT_MUSIC_CATEGORY = -1;

    public static final int NEWEST_MUSIC_CATEGORY = -2;

    public static final class VideoConfig {

        public static final long MAX_VIDEO_DURATION = 60 * 1000;

        public static final long MIN_VIDEO_DURATION = 5 * 1000;

        public static final int WIDTH = 720;

        public static final int HEIGHT = 1280;
    }

    public static final class IntentCode {
        public static final int REQUEST_CODE_RECORDING = 0x00;
        public static final int RESULT_CODE_GALLERY_OK = 0x02;
        public static final int RESULT_CODE_GALLERY_CANCEL = 0x03;
        public static final int REQUEST_CODE_GALLERY = 0x04;
        public static final int REQUEST_CODE_PREVIEW = 0x05;
        public static final int REQUEST_CODE_MUSIC = 0x06;
        public static final int REQUEST_CODE_CUT = 0x07;
        public static final int REQUEST_CODE_EDIT = 0x08;
    }

    public static class BundleConstants {
        public static final String WAVE_DATA_URL = "wave_data_url";
        public static final String MUSIC_NAME = "music_name";
        public static final String AUDIO_URL = "audio_url";
        public static final String RESULT_VIDEO_PATH = "final video path";
        public static final String RESULT_VIDEO_DURATION = "return duration";
        public static final String RESULT_VIDEO_WIDTH = "return width";
        public static final String RESULT_VIDEO_HEIGHT = "return height";
        public static final String RESULT_VIDEO_THUMB = "return thumbnail";
        public static final String RESULT_VIDEO_MAIN_COLOR = "return main color";
        public static final String CUT_VIDEO_PATH = "cut video path";
    }

    public static final class BroadcastAction {
    }

    public static final class EncodeConfig {
        /**
         * How long to wait for the next buffer to become available.
         */
        public static final int TIMEOUT_USEC = 10000;

        // parameters for the video encoder
        public static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
        public static int OUTPUT_VIDEO_BIT_RATE = 3500000; // 2Mbps
        public static int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
        public static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 1; // 1 seconds between I-frames
        public static final int OUTPUT_VIDEO_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

        // parameters for the audio encoder
        public static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
        public static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
        public static final int OUTPUT_AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO; //音频录制通道,默认为立体声
        public static final int OUTPUT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; //音频录制格式，默认为PCM16Bit
        public static final int OUTPUT_AUDIO_AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
        public static final int OUTPUT_AUDIO_BIT_RATE = 128000;//音频编码的密钥比特率
//        public static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 48000; // Must match the input stream.
        public static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.
    }
}
