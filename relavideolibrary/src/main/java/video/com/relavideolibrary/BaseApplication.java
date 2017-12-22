package video.com.relavideolibrary;

import android.app.Application;
import android.content.Context;

import com.kingsoft.media.httpcache.KSYProxyService;

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

    private static KSYProxyService proxyService = null;
    private static Context cc;
    private static BaseApplication app = null;

    public static KSYProxyService getKSYProxy(Context context) {
        cc = context;
        if (app == null) {
            app = new BaseApplication();
        }
        return app.proxyService == null ? (app.proxyService = newKSYProxy()) : app.proxyService;
    }

    private static KSYProxyService newKSYProxy() {
        return new KSYProxyService(cc);
    }
}
