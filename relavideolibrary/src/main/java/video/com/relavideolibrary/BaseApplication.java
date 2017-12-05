package video.com.relavideolibrary;

import android.app.Application;
import android.content.Context;

/**
 * Created by chad
 * Time 17/12/5
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class BaseApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
