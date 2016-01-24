package com.example.hadabele.quantipig;

import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/********
 *
 *  Datei...: CameraListener.java
 *  Autor...: HaDaLeBe
 *  Datum...: 29.12.2015
 *
 ********/

public class CameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {
    int opencvheight;
    int opencvwidth;

    @Override
    public void onCameraViewStarted( int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame( CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        opencvheight = mRgba.height();
        opencvwidth = mRgba.width();
        Log.d( "CV-Abmessungen: ", "Höhe: " + Integer.toString( opencvheight) + " | Breite: " + Integer.toString( opencvwidth));

        return mRgba;
    }
}