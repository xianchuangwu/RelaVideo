package video.com.relavideolibrary;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class BaseActivity extends AppCompatActivity implements DialogInterface.OnCancelListener {

    /**
     * 应用是否在前台
     */
    protected boolean isForeground = false;

    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.processing));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog.isShowing())
            dismissDialog();
        mProgressDialog = null;
    }

    /**
     * 设置沉浸式
     *
     * @param hasFocus
     */
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }

    /**
     * 动态设置沉浸式
     *
     * @param on
     */
    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public void setTranslucentBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //6.0修改状态栏字体色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.0 以上直接设置状态栏颜色
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white_10_alpha));
            }
        }
    }

    private int colorId = -1;

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void showTranslucentView() {

        View view = findViewById(R.id.translucent_view);
        if (view != null) {
            if (colorId != -1) {
                view.setBackgroundColor(getResources().getColor(colorId));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    public static boolean isRunningForeground() {
        if (RelaVideoSDK.context == null) return false;
        String packageName = RelaVideoSDK.context.getPackageName();
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
        if (RelaVideoSDK.context != null) {
            ActivityManager activityManager = (ActivityManager) (RelaVideoSDK.context.getSystemService(Context.ACTIVITY_SERVICE));
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
            if (runningTaskInfos != null) {
                ComponentName f = runningTaskInfos.get(0).topActivity;
                topActivityClassName = f.getClassName();
            }
        }
        return topActivityClassName;
    }

    protected void showDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }
    }

    protected void showProgressDialog() {
        if (mProgressDialog != null && !mProgressDialog.isShowing()) {
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
    }

    protected void setDialogProgress(int progress) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.setProgress(progress);
    }

    protected void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }
}
