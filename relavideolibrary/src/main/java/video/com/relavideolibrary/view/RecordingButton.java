package video.com.relavideolibrary.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chad
 * Time 17/12/22
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RecordingButton extends android.support.v7.widget.AppCompatImageButton {

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
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setClickZoomEffect(v);
            }
        });
    }

    public void setClickZoomEffect(final View view) {
        if (view != null) {
            view.setOnTouchListener(new OnTouchListener() {
                boolean cancelled;
                Rect rect = new Rect();

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (onRecordingListener != null) onRecordingListener.startRecording();
                            scaleTo(v, 1.2f);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (rect.isEmpty()) {
                                v.getDrawingRect(rect);
                            }
                            if (!rect.contains((int) event.getX(), (int) event.getY())) {
                                scaleTo(v, 1);
                                cancelled = true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            if (!cancelled) {
                                if (onRecordingListener != null)
                                    onRecordingListener.stopRecording();
                                scaleTo(v, 1);
                            } else {
                                cancelled = false;
                            }
                            break;
                        }
                    }
                    return false;
                }
            });
        }
    }

    private void scaleTo(View v, float scale) {
        v.setScaleX(scale);
        v.setScaleY(scale);
    }

    private OnRecordingListener onRecordingListener;

    public interface OnRecordingListener {
        void startRecording();

        void stopRecording();
    }
}
