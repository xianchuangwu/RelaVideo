package video.com.relavideolibrary;

import video.com.relavideolibrary.interfaces.FilterDataCallback;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RelaVideoSDK {

    public static void initialization(FilterDataCallback filterDataCallback) {
        callback = filterDataCallback;
    }

    private static FilterDataCallback callback;

    public static FilterDataCallback getCallback() {
        return callback;
    }
}
