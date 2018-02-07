package video.com.relavideolibrary.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import video.com.relavideolibrary.R;

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
    public boolean onTouch(final View v, MotionEvent event) {
        //按下操作
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            scaleTo(RecordingButton.this, 1.2f);
            handler.sendEmptyMessage(MSG_DOWN);
        }
        //抬起操作
        if (event.getAction() == MotionEvent.ACTION_UP) {
            scaleTo(RecordingButton.this, 1.0f);
            handler.sendEmptyMessage(MSG_UP);
        }
        return false;
    }

    private final int MSG_DOWN = 0;
    private final int MSG_UP = 1;
    private final int MSG_TIME_PROTECT = 2;
    private boolean is500msProtection = false;//500ms时间保护，保证每段视频至少500ms
    private boolean isUp = false;
    private Toast toast = null;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWN:
                    if (is500msProtection) {
                        if (toast == null) {
                            toast = Toast.makeText(RecordingButton.this.getContext(), RecordingButton.this.getContext().getString(R.string.click_fast), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                        }
                        toast.show();
                        break;
                    }
                    isUp = false;
                    if (onRecordingListener != null) onRecordingListener.startRecording();
                    handler.sendEmptyMessageDelayed(MSG_TIME_PROTECT, 500);
                    is500msProtection = true;
                    break;
                case MSG_UP:
                    isUp = true;
                    if (!is500msProtection && onRecordingListener != null)
                        onRecordingListener.stopRecording();
                    break;
                case MSG_TIME_PROTECT:
                    is500msProtection = false;
                    if (isUp && onRecordingListener != null)
                        onRecordingListener.stopRecording();
                    break;
            }
            return true;
        }
    });

    public interface OnRecordingListener {
        void startRecording();

        void stopRecording();
    }
}
