package video.com.relavideolibrary;

import android.content.Context;

import com.kingsoft.media.httpcache.KSYProxyService;

import java.util.Map;

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

    public RelaVideoSDK init(Context context) {
        RelaVideoSDK.context = context;
        return this;
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
