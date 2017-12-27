package video.com.relavideolibrary.camera.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cj on 2017/6/28.
 * desc
 */

public class DateUtils {

    public static String covertToDate(long duration){
        Date date = new Date(duration);
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(date);
    }

    /**
     * 毫秒转00:00:00
     * @param time
     * @return
     */
    public static String timeFormat(long time) {

        long timeStamp = time / 1000;

        long min = timeStamp / 60;

        long hour = min / 60;

        String hourStr;
        if (hour < 10) {
            hourStr = "0" + hour;
        } else {
            hourStr = hour + "";
        }

        String minStr;
        if (min >= 10) {

            minStr = min % 60 + "";
        } else {
            minStr = "0" + min;
        }

        long sec = timeStamp % 60;

        String secStr;
        if (sec >= 10) {
            secStr = sec + "";
        } else {
            secStr = "0" + sec;
        }

        return hourStr + ":" + minStr + ":" + secStr;
    }
}
