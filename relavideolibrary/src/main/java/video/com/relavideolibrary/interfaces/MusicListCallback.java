package video.com.relavideolibrary.interfaces;

import java.util.List;

import video.com.relavideolibrary.model.MusicBean;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public interface MusicListCallback {

    List<MusicBean> getMusicList(int category);
}
