package video.com.relavideodemo.imageviewer.dragger;


/**
 * 监听图片被拖拽时的状态
 */
public interface ImageDraggerStateListener {

    /**
     * @param state {@link ImageDraggerState}
     */
    void onImageDraggerState(int state);
}
