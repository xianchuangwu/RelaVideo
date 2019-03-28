package video.com.relavideodemo.imageviewer.listener;


import video.com.relavideodemo.imageviewer.widget.ScaleImageView;

/**
 * 图片的切换监听事件
 */
public interface OnImageChangedListener {

    void onImageSelected(int position, ScaleImageView view);
}
