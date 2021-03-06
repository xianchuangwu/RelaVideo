package video.com.relavideolibrary.camera.gpufilter.filter;

import android.opengl.GLES20;

import video.com.relavideolibrary.camera.PrepreadShader;
import video.com.relavideolibrary.camera.gpufilter.basefilter.GPUImageFilter;


/**
 * Created by cj on 2017/5/22.
 * 美白的filter
 */
public class MagicBeautyFilter extends GPUImageFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private int mLevel;

    public MagicBeautyFilter(){
        super(NO_FILTER_VERTEX_SHADER , PrepreadShader.F_BEAUTY.toString());
    }

    protected void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setBeautyLevel(0);//beauty Level
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public void setBeautyLevel(int level){
//        mLevel=level;
//        switch (level) {
//            case 1:
//                setFloat(mParamsLocation, 1.0f);
//                break;
//            case 2:
//                setFloat(mParamsLocation, 0.8f);
//                break;
//            case 3:
//                setFloat(mParamsLocation,0.6f);
//                break;
//            case 4:
//                setFloat(mParamsLocation, 0.4f);
//                break;
//            case 5:
//                setFloat(mParamsLocation,0.33f);
//                break;
//            default:
//                break;
//        }
        final float[][] beautify_level = {
                { 1.0f, 1.0f, 0.15f, 0.15f },
                { 0.8f, 0.9f, 0.2f, 0.2f },
                { 0.6f, 0.8f, 0.25f, 0.25f },
                { 0.4f, 0.7f, 0.38f, 0.3f },
                { 0.33f, 0.63f, 0.4f, 0.35f }
        };
        mLevel = level < 0 ? 0 : (level > 4 ? 4 : level);
        if (level == 0) return;
        setFloatVec4(mParamsLocation, beautify_level[mLevel - 1]);
    }
    public int getBeautyLevel(){
        return mLevel;
    }
    public void onBeautyLevelChanged(){
        setBeautyLevel(3);//beauty level
    }
}
