package video.com.relavideolibrary.jni;

public class RelaVideoJniUtils {
    static {
        System.loadLibrary("rela-video-lib");
    }

    /**
     * 音频混合
     *
     * @param sourceA
     * @param sourceB
     * @param dst
     * @param firstVol
     * @param secondVol
     * @return
     */
    public static native byte[] audioMix(
            byte[] sourceA,
            byte[] sourceB,
            byte[] dst,
            float firstVol,
            float secondVol
    );
}
