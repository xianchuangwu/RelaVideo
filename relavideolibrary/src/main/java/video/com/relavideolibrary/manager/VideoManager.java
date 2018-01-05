package video.com.relavideolibrary.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import video.com.relavideolibrary.model.MusicBean;
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
        musicBean = new MusicBean();
        videoVolumn = 1.0f;
        musicVolumn = 1.0f;
    }

    private VideoBean videoBean;

    public void setVideoBean(@NonNull VideoBean videoBean) {
        this.videoBean = videoBean;
    }

    public VideoBean getVideoBean() {
        return videoBean;
    }

    private MusicBean musicBean;

    public void setMusicBean(@Nullable MusicBean musicBean) {
        this.musicBean = musicBean;
    }

    public MusicBean getMusicBean() {
        return musicBean;
    }

    private float videoVolumn;

    public void setVideoVolumn(float volumn) {
        this.videoVolumn = volumn;
    }

    public float getVideoVolumn() {
        return videoVolumn;
    }

    private float musicVolumn;

    public void setMusicVolumn(float volumn) {
        this.musicVolumn = volumn;
    }

    public float getMusicVolumn() {
        return musicVolumn;
    }

    public void clean() {
        instance = null;
    }

}
