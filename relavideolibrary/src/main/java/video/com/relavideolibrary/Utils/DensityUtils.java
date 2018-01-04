package video.com.relavideolibrary.Utils;

import android.util.TypedValue;

import java.util.Formatter;

import video.com.relavideolibrary.RelaVideoSDK;

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
                dpVal, RelaVideoSDK.context.getResources().getDisplayMetrics());
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

    public static String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public static String stringForTimeFFmpeg(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    public static String HYYstringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        Formatter mFormatter = new Formatter();
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else if (minutes > 0) {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d", seconds).toString();
        }
    }
}
