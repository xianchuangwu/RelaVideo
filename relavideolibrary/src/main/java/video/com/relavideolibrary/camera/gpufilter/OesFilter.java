package video.com.relavideolibrary.camera.gpufilter;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import video.com.relavideolibrary.camera.PrepreadShader;


/**
 * Description: 加载默认的滤镜的filter
 */
public class OesFilter extends AFilter{

    public OesFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgram(PrepreadShader.V_OES_DEFAULT.toString(), PrepreadShader.F_OES_DEFAULT.toString());
    }

    @Override
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,getTextureId());
        GLES20.glUniform1i(mHTexture,getTextureType());
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }

}
