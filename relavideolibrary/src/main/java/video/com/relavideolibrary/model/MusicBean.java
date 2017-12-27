package video.com.relavideolibrary.model;

import java.io.Serializable;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class MusicBean implements Serializable {

    public int musicId;

    public String name;

    public String singer;

    public int musicHours;

    public String url;

    public String categoryName;

    public int collectStatus;

    public boolean isPause;

    public long startTime;

    public long endTime;
}
