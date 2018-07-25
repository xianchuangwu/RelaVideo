package video.com.relavideolibrary.camera;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import baidu.facedetect.FaceDetectManager;

/**
 * Created by chad
 * Time 18/6/25
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */
public class FaceDetectCameraView extends CameraView implements Handler.Callback {

    private FaceDetectManager faceDetectManager;

    private boolean mDetectStoped = true;

    public FaceDetectCameraView(Context context) {
        this(context, null);
    }

    public FaceDetectCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCameraController.setDisplayOrientation(((Activity) context).getWindowManager().getDefaultDisplay().getRotation());
        faceDetectManager = new FaceDetectManager(context, new Handler(this));
        //test open
//        setEyesScale(100);
//        setFaceScale(100);
    }

    private long cameraLastTime;

    @Override
    public void onPreviewFrame(byte[] data, int rotation, int width, int height) {
        super.onPreviewFrame(data, rotation, width, height);
        Log.i("fps", "camera fps:" + 1000 / (System.currentTimeMillis() - cameraLastTime));
        cameraLastTime = System.currentTimeMillis();
        if (!mDetectStoped) {
            faceDetectManager.onFrameAvailable(data, rotation, width, height);
        }
    }

    private long faceLastTime;

    @Override
    public boolean handleMessage(Message msg) {
        //status==0表示识别成功
        FaceDetectManager.FaceData faceData = (FaceDetectManager.FaceData) msg.obj;
        if (faceData.status == 0 && faceData.faces != null && faceData.frame != null) {
            Log.i("fps", "face fps:" + 1000 / (System.currentTimeMillis() - faceLastTime));
            faceLastTime = System.currentTimeMillis();
            int[] landMask = faceData.faces[0].landmarks;
            mCameraRenderer.onDetectFacePoint(faceData.status, landMask, faceData.frame.getWidth(), faceData.frame.getHeight());
        } else if (faceData.status == 6) {
//            Log.d("FaceDetectCameraView", "after no face time:" + System.currentTimeMillis());
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mDetectStoped) {
//            faceDetectManager.start();
//            mDetectStoped = false;
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        faceDetectManager.stop();
//        mDetectStoped = true;
    }

    /**
     * @param scale 0-100
     */
    public void setEyesScale(float scale) {
        mCameraRenderer.setEyesScale(scale);
    }

    /**
     * @param scale 0-100
     */
    public void setFaceScale(float scale) {
        mCameraRenderer.setFaceScale(scale);
    }
}
