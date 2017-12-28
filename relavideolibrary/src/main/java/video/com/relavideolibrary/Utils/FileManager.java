package video.com.relavideolibrary.Utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by chad
 * Time 17/12/28
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class FileManager {

    public static String getRelaStorageFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append("/Rela");
        File file = new File(sb.toString());
        if (!file.exists()) file.mkdirs();
        return sb.toString();
    }

    public static String getVideoFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(getRelaStorageFile());
        sb.append("/video");
        sb.append('-');
        sb.append(System.currentTimeMillis());
        sb.append(".mp4");
        return sb.toString();
    }

    public static String getOtherFile(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(getRelaStorageFile());
        sb.append("/other");
        sb.append('-');
        sb.append(fileName);
        return sb.toString();
    }

    public static String getMusicPath() {

        File file1 = new File(getRelaStorageFile() + "/Music");
        if (!file1.exists()) {
            file1.mkdir();
        }
        return file1.getAbsolutePath();
    }
}
