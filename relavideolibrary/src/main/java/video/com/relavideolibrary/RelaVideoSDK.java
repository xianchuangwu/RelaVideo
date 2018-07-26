package video.com.relavideolibrary;

import android.content.Context;

import com.baidu.idl.facesdk.FaceTracker;
import com.kingsoft.media.httpcache.KSYProxyService;

import java.util.Map;

import baidu.facedetect.Config;
import baidu.facedetect.FaceEnvironment;
import baidu.facedetect.FaceSDKManager;
import video.com.relavideolibrary.camera.utils.Constants;
import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description:
 */

public class RelaVideoSDK {

    public static Context context;

    private static KSYProxyService proxyService = null;

    public static void init(Context context) {
        RelaVideoSDK.context = context;
        Constants.getInstance().setContext(context);

        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance().init(context, Config.licenseID, Config.licenseFileName);
        setFaceConfig(context);
    }

    private static void setFaceConfig(Context context) {
        FaceTracker tracker = FaceSDKManager.getInstance().getFaceTracker(context);
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整

        // 模糊度范围 (0-1) 推荐小于0.7
        tracker.set_blur_thr(FaceEnvironment.VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        tracker.set_illum_thr(FaceEnvironment.VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        tracker.set_cropFaceSize(FaceEnvironment.VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        tracker.set_eulur_angle_thr(FaceEnvironment.VALUE_HEAD_PITCH, FaceEnvironment.VALUE_HEAD_ROLL,
                FaceEnvironment.VALUE_HEAD_YAW);

        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        tracker.set_min_face_size(FaceEnvironment.VALUE_MIN_FACE_SIZE);
        //
        tracker.set_notFace_thr(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        tracker.set_occlu_thr(FaceEnvironment.VALUE_OCCLUSION);
        // 是否进行质量检测
        tracker.set_isCheckQuality(true);
        // 是否进行活体校验
        tracker.set_isVerifyLive(false);
    }

    public static KSYProxyService getKSYProxy() {
        return proxyService == null ? (proxyService = new KSYProxyService(context)) : proxyService;
    }

    /**
     * 滤镜lookup图回调
     *
     * @param filterDataCallback
     * @return
     */
    public RelaVideoSDK addFilter(FilterDataCallback filterDataCallback) {
        Map<String, Object> callbackMap = CallbackManager.getInstance().getCallbackMap();
        callbackMap.put(FilterDataCallback.class.getSimpleName(), filterDataCallback);
        return this;
    }

    /**
     * 音乐类别名称列表回调
     *
     * @param musicCategoryCallback
     * @return
     */
    public RelaVideoSDK addMusicCategory(MusicCategoryCallback musicCategoryCallback) {
        Map<String, Object> callbackMap = CallbackManager.getInstance().getCallbackMap();
        callbackMap.put(MusicCategoryCallback.class.getSimpleName(), musicCategoryCallback);
        return this;
    }

    /**
     * 音乐列表回调
     *
     * @param musicListCallback
     * @return
     */
    public RelaVideoSDK addMusicList(MusicListCallback musicListCallback) {
        Map<String, Object> callbackMap = CallbackManager.getInstance().getCallbackMap();
        callbackMap.put(MusicListCallback.class.getSimpleName(), musicListCallback);
        return this;
    }
}
