/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package baidu.facedetect;

/**
 * 当图片源有新的图片帧时会回调该类。
 */
public interface OnImageFrameAvailableListener {
    void onFrameAvailable(byte[] bytes, int rotation, int width, int height);
}
