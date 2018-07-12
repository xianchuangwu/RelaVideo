package video.com.relavideolibrary.interfaces;

/**
 * Created by chad
 * Time 18/6/28
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */
public interface OnFaceDetectPointListener {
    /**
     * 面部特征点识别
     *
     * @param landmarks
     */
    void onDetectFacePoint(int status, int[] landmarks,int imageWidth,int imageHeight);
}
