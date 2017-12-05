package video.com.relavideolibrary.Utils;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

/**
 * Created by chad
 * Time 17/12/4
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class CameraManager implements SurfaceHolder.Callback {

    public static final String TAG = "CameraManager";

    private static CameraManager instance;

    private CameraParams cameraParams;

    private Camera mCamera;

    private SurfaceView surfaceView;

    private SurfaceHolder surfaceHolder;

    //视频宽度
    private int mVideoWidth = Constant.WIDTH;
    //视频高度
    private int mVideoHeight = Constant.HEIGHT;

    private boolean isRecodering = false;

    private MediaRecorder mMediaRecorder;

    private CameraManager() {
        cameraParams = new CameraParams();
    }

    public static CameraManager getInstance() {
        if (instance == null) instance = new CameraManager();
        return instance;
    }

    public void openCamera(CameraParams cameraParams, SurfaceView surfaceView) {

        if (cameraParams != null) this.cameraParams = cameraParams;

        this.surfaceView = surfaceView;

        this.surfaceHolder = surfaceView.getHolder();

        // 设置分辨率
        surfaceHolder.setFixedSize(Constant.WIDTH, Constant.HEIGHT);
        // 设置该组件让屏幕不会自动关闭
        surfaceHolder.setKeepScreenOn(true);
        // 针对低于3.0的Android
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        surfaceHolder.addCallback(this);

    }

    public void startPreview() {

        Log.d(TAG, "startPreview");

        try {
            mCamera = Camera.open(cameraParams.cameraId);

            Camera.Parameters parameters = mCamera.getParameters();

            List<String> focusModes = parameters.getSupportedFocusModes();
            //碎片化原因,没有很好的对焦方案 FOCUS_MODE_CONTINUOUS_VIDEO效果相对好些
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            Camera.Size previewSize = CameraHelper.getSupportPreviewSize(parameters);
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            Log.d(TAG, "previewSize:" + previewSize.width + "/" + previewSize.height);

            Camera.Size videoSize = CameraHelper.getSupportVideoSize(parameters);
            mVideoWidth = videoSize.width;
            mVideoHeight = videoSize.height;
            Log.d(TAG, "recoderSize:" + mVideoWidth + "/" + mVideoHeight);

            mCamera.setParameters(parameters);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView.getLayoutParams();
            params.width = DensityUtils.dp2px(previewSize.height);
            params.height = DensityUtils.dp2px(previewSize.width);
            surfaceView.setLayoutParams(params);
            Log.d(TAG, "surfaceViewSize:" + previewSize.width + "/" + previewSize.height);

            mCamera.setPreviewDisplay(surfaceHolder);

            CameraHelper.setCameraDisplayOrientation((Activity) surfaceView.getContext(), cameraParams.cameraId, mCamera);

            mCamera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
            stopPreview();
            throw new RuntimeException("CameraManager mCamera.open() error:" + e.getMessage());
        }
    }

    public void stopPreview() {
        Log.d(TAG, "stopPreview");
        if (mCamera != null) {
            //释放相机时,系统自动会关闭闪光灯,这是需要手动切换闪光灯状态
            if (cameraParams.flashMode == 1) {
                cameraParams.flashMode = 0;
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void switchCamera() {
        stopPreview();
        cameraParams.cameraId = (cameraParams.cameraId + 1) % Camera.getNumberOfCameras();
        startPreview();
    }

    public void recoder() {
        if (!isRecodering) {
            isRecodering = true;
            //初始化一个MediaRecorder
            mMediaRecorder = new MediaRecorder();
            //setCamera之前,要先解锁相机,否则可能会报错
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            // 这两项需要放在setOutputFormat之前
            // 设置从麦克风采集声音(或来自录像机的声音AudioSource.CAMCORDER)
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置从摄像头采集图像
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //声道数
            CamcorderProfile profile = null;
            //TODO 如果手机录制不支持16/9,这里录制暂时未做剪裁,播放的时候会出现视频被剪裁
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            }
            mMediaRecorder.setProfile(profile);

            mMediaRecorder.setAudioChannels(cameraParams.audioChannels);

            //设置视频分辨率，设置错误调用start()时会报错
            mMediaRecorder.setVideoSize(mVideoWidth, mVideoHeight);
            Log.d(TAG, "recoder size:" + mVideoWidth + "/" + mVideoHeight);
            // 设置视频帧率，要设置手机支持的帧率,否则就崩溃,使用默认值
//        mMediaRecorder.setVideoFrameRate(25);
            //提高帧频率，录像模糊，花屏，绿屏可写上调试
            mMediaRecorder.setVideoEncodingBitRate(cameraParams.videoBitRate);

            mMediaRecorder.setOrientationHint(CameraHelper.getCameraOrientation(cameraParams.cameraId));

            mMediaRecorder.setOutputFile(cameraParams.outputPath);
            //设置最大录制时常
            mMediaRecorder.setMaxDuration(cameraParams.maxDuration);
            //设置视频预览
            mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    // 发生错误，停止录制
                    if (mMediaRecorder != null) {
                        mMediaRecorder.stop();
                        mMediaRecorder.release();
                        mMediaRecorder = null;
                        Log.i("recoder", "Error");
                    }
                }
            });
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    Log.i("recoder", "recoder complete");
                }
            });
            try {
                // 准备录制
                mMediaRecorder.prepare();
                // 开始录制
                mMediaRecorder.start();
                Log.d(TAG, "MediaRecoder:" + mMediaRecorder.toString());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            isRecodering = false;
            releaseRecoder();
        }
    }

    private void releaseRecoder() {
        if (mMediaRecorder != null) {
            //stop之前先释放这三个回调
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            //先停止
            mMediaRecorder.stop();
            // 在重置mediarecorder
            mMediaRecorder.reset();
            // 释放资源
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mCamera != null)
                mCamera.lock();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceHolder = null;
        stopPreview();
        releaseRecoder();
    }

    public static class CameraParams {
        /**
         * 摄像头id
         */
        public int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;//默认后置摄像头

        public int width = Constant.WIDTH;//默认1280

        public int height = Constant.HEIGHT;//默认720

        public int videoBitRate = 3 * 1024 * 1024;

        public String outputPath = "/storage/emulated/0/rela.mp4";

        public int maxDuration = 60 * 1000;

        public int audioSampleRate = 128 * 1024;

        public int audioChannels = 2;

        //闪光灯模式 0:关闭 1: 开启 2: 自动
        public int flashMode = 0;

        public static CameraParams fromJson(String json) {
            return new Gson().fromJson(json, CameraParams.class);
        }

        public static String toJson(CameraParams cameraParams) {
            return new Gson().toJson(cameraParams);
        }
    }

}
