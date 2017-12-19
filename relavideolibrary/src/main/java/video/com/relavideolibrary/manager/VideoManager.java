package video.com.relavideolibrary.manager;

import android.support.annotation.NonNull;

import video.com.relavideolibrary.model.VideoBean;

/**
 * Created by chad
 * Time 17/12/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class VideoManager {

    private static VideoManager instance;

    public static synchronized VideoManager getInstance() {
        if (instance == null) instance = new VideoManager();
        return instance;
    }

    private VideoManager() {
        videoBean = new VideoBean();
    }

    private VideoBean videoBean;

    public void setVideoBean(@NonNull VideoBean videoBean) {
        this.videoBean = videoBean;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }
}
