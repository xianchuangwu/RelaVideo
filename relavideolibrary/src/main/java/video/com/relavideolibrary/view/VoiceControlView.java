package video.com.relavideolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import video.com.relavideolibrary.R;


/**
 * Created by liuyun on 2017/8/7.
 */

public class VoiceControlView extends View {

    private static final String TAG = "VoiceControlView";

    private Paint mPaint;

    private Paint mPaint1;

    private Paint mPaint2;

    private RectF mRectF;

    private RectF mLayerRectF;

    private Region mRegion;

    private Region circleRegion;

    private int roundRectHeight = dp2px(4);

    private Path circlePath;

    private float mOffSet = 0;

    public VoiceControlView(Context context) {
        super(context);
        init();
    }

    public VoiceControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mRectF = new RectF();

        mLayerRectF = new RectF();

        mRegion = new Region();

        circleRegion = new Region();

        circlePath = new Path();

        mPaint = new Paint();
        mPaint.setColor(getContext().getResources().getColor(R.color.white_gray));
        mPaint.setStrokeWidth(1f);
        mPaint.setAntiAlias(true);

        mPaint1 = new Paint();
        mPaint1.setColor(getContext().getResources().getColor(R.color.rela_color));
        mPaint1.setStrokeWidth(1f);
        mPaint1.setAntiAlias(true);

        mPaint2 = new Paint();
        mPaint2.setColor(getContext().getResources().getColor(R.color.white));
        mPaint2.setStrokeWidth(1f);
        mPaint2.setAntiAlias(true);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float halfRound = roundRectHeight / 2;

        float halfHeight = getMeasuredHeight() / 2;

        mRectF.set(getMeasuredHeight() / 2, halfHeight - halfRound, getMeasuredWidth() - halfRound - getMeasuredHeight() / 2, halfHeight + halfRound);

        float circleX = getMeasuredWidth() / 2 + mOffSet;

        if (circleX <= (getMeasuredHeight() / 2)) {
            mOffSet = getMeasuredHeight() / 2 - getMeasuredWidth() / 2;
        }

        if (circleX >= getMeasuredWidth() - getMeasuredHeight() / 2) {
            mOffSet = getMeasuredWidth() / 2 - getMeasuredHeight() / 2;
        }

        if (mOffSet > 0) {
            mLayerRectF.set(getMeasuredWidth() / 2, halfHeight - halfRound, getMeasuredWidth() / 2 + mOffSet, halfHeight + halfRound);
        } else if (mOffSet < 0) {
            mLayerRectF.set(getMeasuredWidth() / 2 + mOffSet, halfHeight - halfRound, getMeasuredWidth() / 2, halfHeight + halfRound);
        } else {
            mLayerRectF.set(0, halfHeight - halfRound, 0, halfHeight + halfRound);
        }

        canvas.drawRoundRect(mRectF, 5, 5, mPaint);

        canvas.drawRoundRect(mLayerRectF, 5, 5, mPaint1);

        circlePath.reset();

        circlePath.addCircle(getMeasuredWidth() / 2 + mOffSet, halfHeight, halfHeight, Path.Direction.CW);

        circleRegion.set(getMeasuredWidth() / 2 - (int) halfHeight + (int) mOffSet, 0, getMeasuredWidth() / 2 + (int) halfHeight + (int) mOffSet, getMeasuredHeight());

        mRegion.setPath(circlePath, circleRegion);

        canvas.drawPath(circlePath, mPaint2);
    }

    public int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setLayerType(View.LAYER_TYPE_NONE, null);
    }

    float deltaX = 0;

    float lastX = 0;

    boolean contains = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:

                lastX = event.getX();

                deltaX = -1;

                contains = circleRegion.contains((int) event.getX(), (int) event.getY());

                break;

            case MotionEvent.ACTION_MOVE:

                float moveX = event.getX();

                deltaX = moveX - lastX;

                if (contains) {
                    mOffSet += deltaX;

                    setVolume(mOffSet);

                    invalidate();
                }

                lastX = moveX;

                break;
            case MotionEvent.ACTION_UP:
                contains = false;
                deltaX = -1;
                break;
        }
        return true;
    }

    private void setVolume(float offSet) {

        float halfMeasuredWidth = (getMeasuredWidth() - getHeight()) / 2;

        float currentPercent = offSet / halfMeasuredWidth;

        float audioVolume = 0;

        float videoVolume = 0;

        if (currentPercent > 0) {
            audioVolume = 1;
            videoVolume = 1 - currentPercent;
        } else if (currentPercent < 0) {
            audioVolume = 1 + currentPercent;
            videoVolume = 1;

        } else {
            audioVolume = 1;
            videoVolume = 1;
        }

        Log.d(TAG, " currentPercent : " + currentPercent);

        if (audioVolume > 1) {
            audioVolume = 1;
        }

        if (audioVolume < 0) {
            audioVolume = 0;
        }

        if (videoVolume > 1) {
            videoVolume = 1;
        }

        if (videoVolume < 0) {
            videoVolume = 0;
        }

        if (mOnVolumeListener != null) {
            mOnVolumeListener.onVolume(audioVolume, videoVolume);
        }
    }

    private OnVolumeListener mOnVolumeListener;

    public void setOnVolumeListener(OnVolumeListener mOnVolumeListener) {
        this.mOnVolumeListener = mOnVolumeListener;
    }

    public interface OnVolumeListener {
        void onVolume(float audioVolume, float videoVolume);
    }

}
