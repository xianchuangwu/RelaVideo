package video.com.relavideolibrary.Utils;

import android.util.TypedValue;

import video.com.relavideolibrary.BaseApplication;

/**
 * Created by chad
 * Time 17/12/5
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class DensityUtils {

    /**
     * dp转px
     *
     * @param dpVal
     * @return
     */
    public static int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, BaseApplication.context.getResources().getDisplayMetrics());
    }
}
