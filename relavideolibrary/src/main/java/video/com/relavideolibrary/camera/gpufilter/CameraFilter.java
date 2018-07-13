package video.com.relavideolibrary.camera.gpufilter;

import android.content.res.Resources;

import video.com.relavideolibrary.camera.VertexPosition;


/**
 * Description:
 */
public class CameraFilter extends OesFilter {

    public CameraFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    public void setFlag(int flag) {
        super.setFlag(flag);
        float[] coord;
        if (getFlag() == 1) {
            if (android.os.Build.MODEL.contains("Nexus")) {
                coord = VertexPosition.Left.matrix();
            } else {
                coord = VertexPosition.Right.matrix();
            }
        } else {
            if (android.os.Build.MODEL.contains("Nexus")) {
                coord = VertexPosition.Right_And_Flip_Horizontal.matrix();
            } else {
                coord = VertexPosition.Left_And_Flip_Horizontal.matrix();
            }
        }
        mTexBuffer.clear();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }
}
