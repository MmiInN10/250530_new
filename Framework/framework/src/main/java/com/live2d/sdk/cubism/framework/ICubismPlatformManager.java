package com.live2d.sdk.cubism.framework;

import com.live2d.sdk.cubism.framework.motion.ICubismMotionEventFunction;

public interface ICubismPlatformManager {
    void log(String message);
    void printLog(String message);
    void printWarning(String message);
    void printError(String message);
    ICubismMotionEventFunction createMotionEventFunction();
}
