package video.com.relavideolibrary.interfaces;

import java.util.List;

import video.com.relavideolibrary.model.MusicBean;

/**
 * Created by chad
 * Time 17/12/29
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public interface MusicListSyncDataCallback {

    void onSuccess(List<MusicBean> data);

    void onFail();
}
