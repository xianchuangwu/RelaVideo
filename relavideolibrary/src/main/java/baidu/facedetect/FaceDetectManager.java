/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package baidu.facedetect;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.baidu.idl.facesdk.FaceInfo;
import com.baidu.idl.facesdk.FaceSDK;
import com.baidu.idl.facesdk.FaceTracker;

/**
 * 封装了人脸检测的整体逻辑。
 */
public class FaceDetectManager implements OnImageFrameAvailableListener {
    /**
     * 该回调用于回调，人脸检测结果。当没有人脸时，infos 为null,status为 FaceDetector.DETECT_CODE_NO_FACE_DETECTED
     */
    private Context mContext;
    private Handler mainHandler;

    public FaceDetectManager(Context context, Handler mainHandler) {
        mContext = context;
        this.mainHandler = mainHandler;
    }

    private HandlerThread processThread;
    private Handler processHandler;
    private byte[] lastData = null;
    private int lastRotation;
    private int lastWidth;
    private int lastHeight;

    public void start() {
        processThread = new HandlerThread("process");
        processThread.setPriority(10);
        processThread.start();
        processHandler = new Handler(processThread.getLooper());
    }

    private Runnable processRunnable = new Runnable() {
        @Override
        public synchronized void run() {
            try {
                if (lastData == null) {
                    return;
                }

                byte[] result = nv12ScaleDown(lastData, lastWidth, lastHeight);
                lastWidth = lastWidth / 2;
                lastHeight = lastHeight / 2;
                lastData = result;

                long begin = System.currentTimeMillis();
                int[] argb = new int[lastWidth * lastHeight];
                lastRotation = lastRotation < 0 ? 360 + lastRotation : lastRotation;
                FaceDetector.yuvToARGB(lastData, lastWidth, lastHeight, argb, lastRotation, 0);

                int[] argbArray = argb;

                // 旋转了90或270度。高宽需要替换
                if (lastRotation % 180 == 90) {
                    int temp = lastWidth;
                    lastWidth = lastHeight;
                    lastHeight = temp;
                }
                lastData = null;

                process(argbArray, lastWidth, lastHeight);
                Log.d("FaceDetectCameraView", "face detect fps:" + 1000 / (System.currentTimeMillis() - begin));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    public void stop() {
        if (processThread != null) {
            processThread.quit();
            processThread = null;
        }
    }

    private byte[] nv12ScaleDown(byte[] bytes, int width, int height) {
        int newWidth = width >> 1;
        int newHeight = height >> 1;
        byte[] result = new byte[bytes.length >> 2];

        final int sourceOffset = width * height;
        final int destOffset = newWidth * newHeight;
        for (int h = 0; h < newHeight; h++) {
            for (int w = 0; w < newWidth; w++) {

                // manipulate y
                byte sourceY = bytes[(w << 1) + h * (width << 1)];
                result[w + h * newWidth] = sourceY;

                // manipulate uv
                if ((w & 0x1) == 0 && (h & 0x1) == 0) {
                    int indexSourceU = (w << 1) + h * width + sourceOffset;
                    int indexSourceV = indexSourceU + 1;
                    int indexDestU = w + (h >> 1) * newWidth + destOffset;

                    result[indexDestU] = bytes[indexSourceU];
                    result[indexDestU + 1] = bytes[indexSourceV];
                }
            }
        }

        return result;
    }

    private void process(int[] argb, int width, int height) {
        int value;

        ImageFrame frame = new ImageFrame();
        frame.setArgb(argb);
        frame.setWidth(width);
        frame.setHeight(height);

        value = FaceSDKManager.getInstance().getFaceTracker(mContext)
                .prepare_max_face_data_for_verify(frame.getArgb(), frame.getHeight(), frame.getWidth(),
                        FaceSDK.ImgType.ARGB.ordinal(), FaceTracker.ActionType.RECOGNIZE.ordinal());
        FaceInfo[] faces = FaceSDKManager.getInstance().getFaceTracker(mContext).get_TrackedFaceInfo();

        Log.d("FaceDetectCameraView", "mainHandler value:" + value);
        if (mainHandler != null && value == 0) {
            mainHandler.sendMessage(mainHandler.obtainMessage(0x02, new FaceData(value, faces, frame)));
        }

        frame.release();
    }

    @Override
    public void onFrameAvailable(byte[] data, int rotation, int width, int height) {
        if (processHandler != null) {
            lastData = data;
            lastRotation = rotation;
            lastWidth = width;
            lastHeight = height;
            processHandler.removeCallbacks(processRunnable);
            processHandler.post(processRunnable);
        }
    }

    public class FaceData {
        public int status;
        public ImageFrame frame;
        public FaceInfo[] faces;

        public FaceData(int status, FaceInfo[] faces, ImageFrame frame) {
            this.status = status;
            this.faces = faces;
            this.frame = frame;
        }

    }
}
