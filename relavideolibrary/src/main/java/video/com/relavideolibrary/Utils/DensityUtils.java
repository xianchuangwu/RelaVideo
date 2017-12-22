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

    /**
     * 毫秒转换成00:00
     *
     * @param time
     * @return
     */
    public static String longToChronometer(long time) {
        if (time <= 0) {
            return "00:00";
        }
        int secondnd = (int) ((time / 1000) / 60);
        int million = (int) ((time / 1000) % 60);
        String f = secondnd >= 10 ? String.valueOf(secondnd) : "0" + String.valueOf(secondnd);
        String m = million >= 10 ? String.valueOf(million) : "0" + String.valueOf(million);
        return f + ":" + m;
    }
}
