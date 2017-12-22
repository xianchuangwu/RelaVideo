package video.com.relavideolibrary.Utils;

/**
 * Created by chad
 * Time 17/12/4
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class Constant {

    public static final int WIDTH = 1280;

    public static final int HEIGHT = 720;

    public static final String KEY_INTENT_CAMERA_PARAMS = "camera params";

    public static final String RECODER_RESULT_PATH = "video path";

    public static final String VIDEO_BEAN = "video bean";

    public static final class IntentCode {
        public static final int REQUEST_CODE_RECODER = 0x00;
        public static final int RESULT_CODE_RECODER = 0x01;
        public static final int RESULT_CODE_GALLERY_OK = 0x02;
        public static final int RESULT_CODE_GALLERY_CANCEL = 0x03;
        public static final int REQUEST_CODE_GALLERY = 0x04;
        public static final int REQUEST_CODE_PREVIEW = 0x05;
        public static final int REQUEST_CODE_MUSIC = 0x06;
        public static final int REQUEST_CODE_CUT = 0x07;
    }

    public static class BundleConstants {
        public static final String WAVE_DATA_URL = "wave_data_url";
        public static final String MUSIC_NAME = "music_name";
        public static final String AUDIO_URL = "audio_url";
    }

    public static final class BroadcastAction {
    }
}
