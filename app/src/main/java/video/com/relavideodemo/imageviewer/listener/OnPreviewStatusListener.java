package video.com.relavideodemo.imageviewer.listener;


import video.com.relavideodemo.imageviewer.ImageViewerState;
import video.com.relavideodemo.imageviewer.widget.ScaleImageView;

/**
 * 监听图片浏览器的状态
 */
public interface OnPreviewStatusListener {

    /**
     * 监听图片预览器的当前状态
     *
     * @param state      图片预览器的当前状态
     * @param imagePager 当前的 itemView
     */
    void onPreviewStatus(@ImageViewerState int state, ScaleImageView imagePager);
}
