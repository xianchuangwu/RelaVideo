package video.com.relavideolibrary.Utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;

/**
 * Created by chad
 * Time 17/6/14
 * Email: wuxianchuang@foxmail.com
 * Description: TODO GPUImage滤镜渲染
 */

public class FilterTransformation extends BitmapTransformation {
    private Context context;

    private int filterId;

    public FilterTransformation(Context context, int filterId) {
        super(context);
        this.context = context;
        this.filterId = filterId;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (filterId == -1) {
            return toTransform;
        }
        GPUImage gpuImage = new GPUImage(context);
        gpuImage.setImage(toTransform);

//        GPUImageFilterGroup gpuImageFilter = new GPUImageFilterGroup();
//        gpuImageFilter.addFilter(new GPUImageExtTexFilter());
//        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
//        lookupFilter.setBitmap(BitmapFactory.decodeResource(context.getResources(), filterId));
//        gpuImageFilter.addFilter(lookupFilter);
//        gpuImage.setFilter(gpuImageFilter);

        gpuImage.setFilter(new GPUImageSobelEdgeDetection());

        return gpuImage.getBitmapWithFilterApplied();
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}
