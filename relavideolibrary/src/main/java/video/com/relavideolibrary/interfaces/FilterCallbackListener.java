package video.com.relavideolibrary.interfaces;

import java.util.List;

import video.com.relavideolibrary.model.FilterBean;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public interface FilterCallbackListener {
    void onComplete(List<FilterBean> filterBeans);
}
