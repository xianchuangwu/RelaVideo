package video.com.relavideolibrary.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;

import video.com.relavideolibrary.R;
import video.com.relavideolibrary.Utils.DensityUtils;

/**
 * Created by chad
 * Time 17/1/17
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 圆角imageView
 */

public class RoundCornersImageView extends android.support.v7.widget.AppCompatImageView {
    private float radiusX = DensityUtils.dp2px(10);
    private float radiusY = DensityUtils.dp2px(10);
    private Path path;
    private Rect rect;
    private RectF rectF;

    public RoundCornersImageView(Context context) {
        super(context, null);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundCornersImageView, defStyleAttr, R.style.AppCompatLightTheme);
        radiusX = a.getDimensionPixelSize(R.styleable.RoundCornersImageView_radius, DensityUtils.dp2px(10));
        radiusY = radiusX;
        a.recycle();
        init();
    }

    private void init() {
        path = new Path();
        rect = new Rect();
        rectF = new RectF();
    }

    public void setRadius(float radiusX, float radiusY) {
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        rect.set(0, 0, getWidth(), getHeight());
        rectF.set(rect);
        path.addRoundRect(rectF, radiusX, radiusY, Path.Direction.CCW);
        canvas.clipPath(path, Region.Op.REPLACE);//Op.REPLACE这个范围内的都将显示，超出的部分覆盖
        super.onDraw(canvas);
    }
}
