package video.com.relavideolibrary;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class BaseActivity extends AppCompatActivity {

    /**
     * 应用是否在前台
     */
    protected boolean isForeground = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * 在这里本来可以使用if (!isAppOnForeground()) {//to do
         * sth}，但为了避免再次调用isRunningForeground()而造成费时且增大系统的开销，故在这里我应用了一个标志位来判断
         */
        if (!isForeground) {
            isForeground = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isRunningForeground()) {
            isForeground = false;
        }
    }

    /**
     * 设置沉浸式
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public static boolean isRunningForeground() {
        String packageName = BaseApplication.context.getPackageName();
        String topActivityClassName = getTopActivityName();
        if (packageName != null && topActivityClassName != null && topActivityClassName.startsWith(packageName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获得当前Activity
     *
     * @return
     */
    public static String getTopActivityName() {
        String topActivityClassName = null;
        ActivityManager activityManager = (ActivityManager) (BaseApplication.context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();
        }
        return topActivityClassName;
    }
}
