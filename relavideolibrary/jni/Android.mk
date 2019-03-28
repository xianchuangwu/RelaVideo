LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gpuimage-library
LOCAL_SRC_FILES := yuv-decoder.c

include $(BUILD_SHARED_LIBRARY)