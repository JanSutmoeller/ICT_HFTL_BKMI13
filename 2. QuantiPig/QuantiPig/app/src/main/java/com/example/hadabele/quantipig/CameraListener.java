package com.example.hadabele.quantipig;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by hadabele on 29.12.2015.
 */
public class CameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        return mRgba;
    }
}
