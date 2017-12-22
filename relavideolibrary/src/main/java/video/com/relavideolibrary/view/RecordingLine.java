package video.com.relavideolibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.DensityUtils;

/**
 * Created by chad
 * Time 17/12/22
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class RecordingLine extends android.support.v7.widget.AppCompatTextView {

    private Context mContext;

    private Paint mPaint = new Paint();

    private int whiteColor;

    private int transColor;

    private int redColor;

    public RecordingLine(Context context) {
        super(context, null);
        mContext = context;
    }

    public RecordingLine(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
    }

    public RecordingLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        whiteColor = ContextCompat.getColor(mContext, R.color.white);
        transColor = ContextCompat.getColor(mContext, R.color.trans_line_color);
        redColor = ContextCompat.getColor(mContext, R.color.red_line_color);
        mPaint.setStrokeWidth(DensityUtils.dp2px(5));
        mPaint.setColor(whiteColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, 500, 0, mPaint);
    }
}
