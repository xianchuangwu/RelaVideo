package video.com.relavideolibrary.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.Constant;

/**
 * Created by chad
 * Time 17/12/22
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RecordingLine extends android.support.v7.widget.AppCompatTextView implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private static final String TAG = "RecordingLine";

    private Context mContext;

    private final long maxDuration = Constant.VideoConfig.MAX_VIDEO_DURATION;
    private long lastDuration;

    private float perMilliSecWidth;//每毫秒宽度

    private float intervalWidth;//间隔宽度

    private Paint mPaint = new Paint();

    private int whiteColor;

    private int transBgColor;

    private int transColor;

    private int redColor;

    private float currentLineX;

    private List<Square> lines = new ArrayList<>();

    private ValueAnimator valueAnimator;

    private boolean willDelete = false;

    public RecordingLine(Context context) {
        super(context, null);
        mContext = context;
        init();
    }

    public RecordingLine(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        init();
    }

    public RecordingLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        whiteColor = mContext.getResources().getColor(R.color.white);
        transBgColor = mContext.getResources().getColor(R.color.trans_line_color);
        transColor = mContext.getResources().getColor(R.color.white_trans);
        redColor = mContext.getResources().getColor(R.color.red_line_color);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关掉硬件加速
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measuredHeight, measuredWidth;
        int SIZE = 10;//控件默认大小

        if (widthMode == MeasureSpec.EXACTLY) {
            measuredWidth = widthSize;
        } else {
            measuredWidth = SIZE;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            measuredHeight = heightSize;
        } else {
            measuredHeight = SIZE;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);

        perMilliSecWidth = (float) measuredWidth / maxDuration;
        intervalWidth = perMilliSecWidth * 300;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawArrayMapLines(canvas);
        drawCurrentLine(canvas);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        lastDuration = (long) value;
        currentLineX = (float) value * perMilliSecWidth;
        Log.d(TAG, "value :" + value + ",lastDuration :" + lastDuration + ",currentLineX :" + currentLineX);
        if (recordingLineListener != null)
            recordingLineListener.recordProgress(lastDuration);
        invalidate();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        float endX;
        float maxX = getMeasuredWidth() - getMeasuredHeight() / 2;//总长度减去末尾半圆
        if (currentLineX <= maxX) {
            endX = currentLineX;
        } else {
            endX = maxX;
        }
        //current line
        mPaint.setColor(whiteColor);
        if (lines.size() == 0) {
            int height = getMeasuredHeight();
            Square square = new Square();
            square.paint = new Paint(mPaint);
            square.recf = new RectF(height / 2, 0, endX, height);
            lines.add(square);
        } else {
            RectF rectF = lines.get(lines.size() - 1).recf;
            Square square = new Square();
            square.paint = new Paint(mPaint);
            square.recf = new RectF(rectF.right, rectF.top, endX, rectF.bottom);
            lines.add(square);
        }
        invalidate();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public void start() {
        valueAnimator = ValueAnimator.ofInt((int) lastDuration, (int) maxDuration);
        valueAnimator.setDuration(maxDuration - lastDuration);
        valueAnimator.setInterpolator(new LinearInterpolator());//匀速运动
        valueAnimator.addUpdateListener(this);
        valueAnimator.addListener(this);
        valueAnimator.start();
    }

    public void stop() {
        valueAnimator.cancel();
    }

    public boolean delete() {
        Log.d(TAG, "delete before:" + String.valueOf(lines.size()));
        if (lines.size() > 0) {
            if (willDelete) {
                lines.remove(lines.size() - 1);
                if (lines.size() > 0) {
                    lastDuration = (long) (lines.get(lines.size() - 1).recf.right / perMilliSecWidth);
                } else {
                    lastDuration = 0;
                }
                if (recordingLineListener != null)
                    recordingLineListener.recordProgress(lastDuration);
            }
            invalidate();
            willDelete = !willDelete;
        }
        Log.d(TAG, "delete after:" + String.valueOf(lines.size()));
        return !willDelete;
    }

    public long getLastDuration() {
        return lastDuration;
    }

    private void drawCurrentLine(Canvas canvas) {
        if (lastDuration < Constant.VideoConfig.MAX_VIDEO_DURATION
                && valueAnimator != null
                && valueAnimator.isRunning()) {
            float height = getMeasuredHeight();
            float width = getMeasuredWidth();

            float endX;
            float maxX = width - height / 2;//总长度减去末尾半圆
            if (currentLineX <= maxX) {
                endX = currentLineX;
            } else {
                endX = maxX;
            }
            mPaint.setColor(whiteColor);
            if (lines.size() == 0) {
                canvas.drawCircle(height / 2, height / 2, height / 2, mPaint);
                canvas.drawRect(height / 2, 0, endX, height, mPaint);
                //画末尾的半圆
                if (currentLineX >= maxX) canvas.drawCircle(maxX, height / 2, height / 2, mPaint);
            } else {
                RectF rectF = lines.get(lines.size() - 1).recf;
                //先画间隔
                mPaint.setColor(transColor);
                canvas.drawRect(rectF.right, rectF.top, rectF.right + intervalWidth, rectF.bottom, mPaint);
                mPaint.setColor(whiteColor);
                canvas.drawRect(rectF.right + intervalWidth, rectF.top, endX, rectF.bottom, mPaint);
                //画末尾的半圆
                if (currentLineX >= maxX) canvas.drawCircle(maxX, height / 2, height / 2, mPaint);
            }
        }
    }

    private void drawArrayMapLines(Canvas canvas) {

        if (lines.size() > 0) {
            Log.d("drawArrayMapLines", String.valueOf(lines.size()));
            float height = getMeasuredHeight();
            float width = getMeasuredWidth();
            Paint paintCircle = lines.get(0).paint;
            if (lines.size() == 1 && willDelete) {
                paintCircle.setColor(redColor);
            }
            canvas.drawCircle(height / 2, height / 2, height / 2, paintCircle);

            float maxX = width - height / 2;//总长度减去末尾半圆
            for (int i = 0; i < lines.size(); i++) {
                RectF rectF = lines.get(i).recf;
                Paint paint = lines.get(i).paint;
                //第一个line前面不画间隔
                if (i == 0) {
                    if (lines.size() == 1 && willDelete) {
                        paint.setColor(redColor);
                    }
                    canvas.drawRect(rectF, paint);
                    if (rectF.right >= maxX)
                        canvas.drawCircle(maxX, height / 2, height / 2, paint);
                } else {
                    mPaint.setColor(transColor);
                    canvas.drawRect(rectF.left, rectF.top, rectF.left + intervalWidth, rectF.bottom, mPaint);//间隔
                    if (i == lines.size() - 1 && willDelete)
                        paint.setColor(redColor);
                    canvas.drawRect(rectF.left + intervalWidth, rectF.top, rectF.right, rectF.bottom, paint);//line
                    if (rectF.right >= maxX)
                        canvas.drawCircle(maxX, height / 2, height / 2, paint);
                }
            }
        }
    }

    private void drawBackground(Canvas canvas) {
        float width = getMeasuredWidth();
        float height = getMeasuredHeight();

        //draw background
        mPaint.setColor(transBgColor);

        //draw left half circle
        canvas.drawCircle(height / 2, height / 2, height / 2, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(height / 2, 0, height, height, mPaint);

        //draw right half circle
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawCircle(Math.abs(width - height / 2), height / 2, height / 2, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(Math.abs(width - height), 0, width - height / 2, height, mPaint);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawRect(height / 2, 0, Math.abs(width - height / 2), height, mPaint);
        mPaint.setColor(whiteColor);
        canvas.drawRect(width / 12, 0, width / 12 + intervalWidth, height, mPaint);//最短时长刻度线,5s时line
    }

    private RecordingLineListener recordingLineListener;

    public void setRecordingLineListener(RecordingLineListener recordingLineListener) {
        this.recordingLineListener = recordingLineListener;
    }

    public interface RecordingLineListener {
        void recordProgress(long progress);
    }

    private class Square {

        public Paint paint;

        public RectF recf;
    }
}
