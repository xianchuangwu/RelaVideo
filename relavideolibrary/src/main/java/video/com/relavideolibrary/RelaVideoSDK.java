package video.com.relavideolibrary;

import video.com.relavideolibrary.interfaces.FilterDataCallback;
import video.com.relavideolibrary.interfaces.MusicCategoryCallback;
import video.com.relavideolibrary.interfaces.MusicListCallback;
import video.com.relavideolibrary.interfaces.MusicPlayEventListener;
import video.com.relavideolibrary.model.MusicProgressEvent;

/**
 * Created by chad
 * Time 17/12/19
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RelaVideoSDK {

    public static void initialization(FilterDataCallback filterDataCallback
            , MusicCategoryCallback musicCategoryCallback
            , MusicListCallback musicListCallback) {
        RelaVideoSDK.filterDataCallback = filterDataCallback;
        RelaVideoSDK.musicCategoryCallback = musicCategoryCallback;
        RelaVideoSDK.musicListCallback = musicListCallback;
    }

    private static FilterDataCallback filterDataCallback;

    public static FilterDataCallback getFilterDataCallback() {
        return filterDataCallback;
    }

    private static MusicCategoryCallback musicCategoryCallback;

    public static MusicCategoryCallback getMusicCategoryCallback() {
        return musicCategoryCallback;
    }

    private static MusicListCallback musicListCallback;

    public static MusicListCallback getMusicListCallback() {
        return musicListCallback;
    }

    private static MusicPlayEventListener musicPlayEventListener;

    public static void setMusicPlayEventListener(MusicPlayEventListener musicPlayEventListener) {
        RelaVideoSDK.musicPlayEventListener = musicPlayEventListener;
    }

    public static MusicPlayEventListener getMusicPlayEventListener() {
        return musicPlayEventListener;
    }
}
