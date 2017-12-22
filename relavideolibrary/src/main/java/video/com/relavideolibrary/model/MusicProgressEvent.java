package video.com.relavideolibrary.model;

import java.io.Serializable;

/**
 * Created by chad
 * Time 17/8/8
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class MusicProgressEvent implements Serializable{

    private int musicId;
    private String musicUrl;
    private int musicPorgress;
    private boolean isLoading;

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public int getMusicPorgress() {
        return musicPorgress;
    }

    public void setMusicPorgress(int musicPorgress) {
        this.musicPorgress = musicPorgress;
    }

    @Override
    public String toString() {
        return "MusicProgressEvent{" +
                "musicId='" + musicId + '\'' +
                ", musicUrl='" + musicUrl + '\'' +
                ", musicPorgress=" + musicPorgress +
                ", isLoading=" + isLoading +
                '}';
    }
}
