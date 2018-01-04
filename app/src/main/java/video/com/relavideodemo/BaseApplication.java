package video.com.relavideodemo;

import android.app.Application;

import video.com.relavideolibrary.RelaVideoSDK;

/**
 * Created by chad
 * Time 17/12/29
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RelaVideoSDK.init(getApplicationContext());
    }
}
