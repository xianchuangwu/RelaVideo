package video.com.relavideolibrary.camera.gpufilter;

import android.content.res.Resources;
import android.opengl.GLES20;

import video.com.relavideolibrary.camera.PrepreadShader;


/**
 * Description:
 */
public class NoFilter extends AFilter {

    public NoFilter(Resources res) {
        super(res);
    }

    @Override
    protected void onCreate() {
//        createProgramByAssetsFile("shader/base_vertex.sh",
//            "shader/base_fragment.sh");
        createProgram(PrepreadShader.V_DEFAULT.toString(), PrepreadShader.F_DEFAULT.toString());
    }

    /**
     * 背景默认为黑色
     */
    @Override
    protected void onClear() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
