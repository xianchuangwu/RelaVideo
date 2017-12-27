package video.com.relavideolibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chad
 * Time 17/12/22
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RecordingButton extends android.support.v7.widget.AppCompatImageButton implements View.OnTouchListener {

    public RecordingButton(Context context) {
        super(context);
    }

    public RecordingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void registerOnClickListener(OnRecordingListener onRecordingListener) {
        this.onRecordingListener = onRecordingListener;
        setOnTouchListener(this);
    }


    private void scaleTo(View v, float scale) {
        v.setScaleX(scale);
        v.setScaleY(scale);
    }

    private OnRecordingListener onRecordingListener;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        //按下操作
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            scaleTo(v, 1.2f);
            if (onRecordingListener != null) onRecordingListener.startRecording();
        }
        //抬起操作
        if (event.getAction() == MotionEvent.ACTION_UP) {
            scaleTo(v, 1.0f);
            if (onRecordingListener != null)
                onRecordingListener.stopRecording();
        }
        return false;
    }

    public interface OnRecordingListener {
        void startRecording();

        void stopRecording();
    }
}
