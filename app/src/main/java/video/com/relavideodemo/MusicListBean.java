package video.com.relavideodemo;

import java.util.List;

import video.com.relavideolibrary.model.MusicBean;

/**
 * Created by chad
 * Time 18/1/30
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class MusicListBean {

    /**
     * 错误标识
     */
    public String errcode = "";

    /**
     * 错误描述
     */
    public String errdesc = "";

    /**
     * 错误描述英文国际化
     */
    public String errdesc_en = "";

    /**
     * 是否成功 (成功为1，失败为0)
     */
    public String result = "";

    public List<MusicBean> data ;
}
