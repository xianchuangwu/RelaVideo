package video.com.relavideolibrary.camera.gpufilter.filter;

import android.opengl.GLES20;

import video.com.relavideolibrary.camera.PrepreadShader;
import video.com.relavideolibrary.camera.gpufilter.basefilter.GPUImageFilter;

/**
 * Created by chad
 * Time 18/6/28
 * Email: wuxianchuang@foxmail.com
 * Description: TODO大眼瘦脸
 */
public class BigEyesThinFaceFilter extends GPUImageFilter {

    private int leftEyePoint_x_location;

    private int leftEyePoint_y_location;

    private int rightEyePoint_x_location;

    private int rightEyePoint_y_location;

    private int eyesScale_location;

    private int faceScale_location;

    private int arraySize_location;

    private int maxFaceWidth_location;

    private int leftContourPoints_location;

    private int rightContourPoints_location;

    public static final float mMinFaceScale = 3f;
    public static final float mMaxFaceScale = 12f;
    public static final float mMinEyesScale = 0f;
    public static final float mMaxEyesScale = 0.25f;

    private float mEyesScale = mMinEyesScale;
    private float mFaceScale = mMinFaceScale;

    public BigEyesThinFaceFilter() {
        super(NO_FILTER_VERTEX_SHADER, PrepreadShader.F_BIG_EYE_AND_THIN_FACE.toString());
    }

    @Override
    protected void onInit() {
        super.onInit();
        maxFaceWidth_location = GLES20.glGetUniformLocation(getProgram(), "maxFaceWidth");
        leftEyePoint_x_location = GLES20.glGetUniformLocation(getProgram(), "leftEyePoint_x");
        leftEyePoint_y_location = GLES20.glGetUniformLocation(getProgram(), "leftEyePoint_y");
        rightEyePoint_x_location = GLES20.glGetUniformLocation(getProgram(), "rightEyePoint_x");
        rightEyePoint_y_location = GLES20.glGetUniformLocation(getProgram(), "rightEyePoint_y");
        eyesScale_location = GLES20.glGetUniformLocation(getProgram(), "eyesScale");
        faceScale_location = GLES20.glGetUniformLocation(getProgram(), "faceScale");
        arraySize_location = GLES20.glGetUniformLocation(getProgram(), "arraySize");
        leftContourPoints_location = GLES20.glGetUniformLocation(getProgram(), "leftContourPoints");
        rightContourPoints_location = GLES20.glGetUniformLocation(getProgram(), "rightContourPoints");
    }

    public void setFaceScale(float scale) {
        this.mFaceScale = scale * mMaxFaceScale / 100;
        if (this.mFaceScale < mMinFaceScale) this.mFaceScale = mMinFaceScale;
    }

    public void setEyesScale(float scale) {
        this.mEyesScale = scale * mMaxEyesScale / 100;
    }

    public void setLandMask(int[] landMask, float imageWidth, float imageHeight) {
        setInteger(arraySize_location, 11);
        setFloat(eyesScale_location, mEyesScale);
        setFloat(faceScale_location, mFaceScale);
        setLeftEyePoint_x(1f - landMask[21 * 2] / imageWidth);
        setLeftEyePoint_y(1f - landMask[21 * 2 + 1] / imageHeight);
        setRightEyePoint_x(1f - landMask[38 * 2] / imageWidth);
        setRightEyePoint_y(1f - landMask[38 * 2 + 1] / imageHeight);

        float faceLeftPoint_x = 1f - landMask[0] / imageWidth;
        float faceLeftPoint_y = 1f - landMask[1] / imageHeight;
        float faceRightPoint_x = 1f - landMask[12 * 2] / imageWidth;
        float faceRightPoint_y = 1f - landMask[12 * 2 + 1] / imageHeight;
        setFaceMaxWidth((float) (Math.sqrt(Math.pow(faceLeftPoint_x - faceRightPoint_x, 2) + Math.pow(faceLeftPoint_y - faceRightPoint_y, 2)) / 6));

        float left1_x = 1f - landMask[2] / imageWidth;
        float left2_x = 1f - landMask[2 * 2] / imageWidth;
        float left3_x = 1f - landMask[3 * 2] / imageWidth;
        float left4_x = 1f - landMask[4 * 2] / imageWidth;
        float left5_x = 1f - landMask[5 * 2] / imageWidth;
        float left6_x = 1f - landMask[6 * 2] / imageWidth;
        float left1_y = 1f - landMask[3] / imageHeight;
        float left2_y = 1f - landMask[2 * 2 + 1] / imageHeight;
        float left3_y = 1f - landMask[3 * 2 + 1] / imageHeight;
        float left4_y = 1f - landMask[4 * 2 + 1] / imageHeight;
        float left5_y = 1f - landMask[5 * 2 + 1] / imageHeight;
        float left6_y = 1f - landMask[6 * 2 + 1] / imageHeight;
        float right11_x = 1f - landMask[11 * 2] / imageWidth;
        float right10_x = 1f - landMask[10 * 2] / imageWidth;
        float right9_x = 1f - landMask[9 * 2] / imageWidth;
        float right8_x = 1f - landMask[8 * 2] / imageWidth;
        float right7_x = 1f - landMask[7 * 2] / imageWidth;
        float right6_x = 1f - landMask[6 * 2] / imageWidth;
        float right11_y = 1f - landMask[11 * 2 + 1] / imageHeight;
        float right10_y = 1f - landMask[10 * 2 + 1] / imageHeight;
        float right9_y = 1f - landMask[9 * 2 + 1] / imageHeight;
        float right8_y = 1f - landMask[8 * 2 + 1] / imageHeight;
        float right7_y = 1f - landMask[7 * 2 + 1] / imageHeight;
        float right6_y = 1f - landMask[6 * 2 + 1] / imageHeight;

        setLeftContourPoints(new float[]{
                left1_x, left1_y,
                (left1_x + left2_x) / 2, (left1_y + left2_y) / 2,
                left2_x, left2_y,
                (left2_x + left3_x) / 2, (left2_y + left3_y) / 2,
                left3_x, left3_y,
                (left3_x + left4_x) / 2, (left3_y + left4_y) / 2,
                left4_x, left4_y,
                (left4_x + left5_x) / 2, (left4_y + left5_y) / 2,
                left5_x, left5_y,
                (left5_x + left6_x) / 2, (left5_y + left6_y) / 2,
                left6_x, left6_y
        });
        setRightContourPoints(new float[]{
                right11_x, right11_y,
                (right11_x + right10_x) / 2, (right11_y + right10_y) / 2,
                right10_x, right10_y,
                (right10_x + right9_x) / 2, (right10_y + right9_y) / 2,
                right9_x, right9_y,
                (right9_x + right8_x) / 2, (right9_y + right8_y) / 2,
                right8_x, right8_y,
                (right8_x + right7_x) / 2, (right8_y + right7_y) / 2,
                right7_x, right7_y,
                (right7_x + right6_x) / 2, (right7_y + right6_y) / 2,
                right6_x, right6_y
        });
    }

    private void setLeftEyePoint_x(float newValue) {
        setFloat(leftEyePoint_x_location, newValue);
    }

    private void setLeftEyePoint_y(float newValue) {
        setFloat(leftEyePoint_y_location, newValue);
    }

    private void setRightEyePoint_x(float newValue) {
        setFloat(rightEyePoint_x_location, newValue);
    }

    private void setRightEyePoint_y(float newValue) {
        setFloat(rightEyePoint_y_location, newValue);
    }

    private void setLeftContourPoints(float[] newValue) {
        setFloatArray(leftContourPoints_location, newValue);
    }

    private void setRightContourPoints(float[] newValue) {
        setFloatArray(rightContourPoints_location, newValue);
    }

    private void setFaceMaxWidth(float newValue) {
        setFloat(maxFaceWidth_location, newValue);
    }
}
