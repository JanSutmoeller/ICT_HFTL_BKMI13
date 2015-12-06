package com.example.hadabele.quantipig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends Activity implements CvCameraViewListener2 {

    Button capture_button, quantimode_button;
    private Mat mCurrentMat;
    private int quantizationMode = 0;

    private static final int QUANT_MODE_0 = 0;
    private static final String QUANT_MODE_0_STRING = "Modus 0";

    private static final int QUANT_MODE_1 = 1;
    private static final String QUANT_MODE_1_STRING = "Helligkeit";

    private static final int QUANT_MODE_2 = 2;
    private static final String QUANT_MODE_2_STRING = "Modus 2";



    public static final String TAG = "MainActivity";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {

                    capture_button = (Button)findViewById(R.id.button_capture);
                    capture_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            takePicture(mCurrentMat);

                        }
                    });
                    quantimode_button =(Button)findViewById(R.id.button_quantimode);
                    quantimode_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createQuantizaionModeMenu();
                        }
                    });
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    private CameraBridgeViewBase mOpenCvCameraView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(TAG, "camera frame");
        mCurrentMat = getQuantizisedImage(inputFrame);
        return mCurrentMat;
    }

    @Override
    public void onStart(){

     super.onStart();
        capture_button = (Button)findViewById(R.id.button_capture);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                takePicture(mCurrentMat);
              // TODO: Crash beim Aufnehmen abfangen!

    }
        });
        quantimode_button =(Button)findViewById(R.id.button_quantimode);
        quantimode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuantizaionModeMenu();
            }
        });

    }



    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    // Methode zum Speichern des Bildes

    public boolean takePicture(Mat pMat){
        Log.d(TAG, "takePicture");

        // Timestamp für Dateinamen
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH_mm_ss");
        //Date date = new Date(System.currentTimeMillis());
        String dateString = sdf.format(c.getTime());

        // Speichern des Bildes unter DCIM/QuantiPig
        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/QuantiPig");
        if(!filePath.exists()){
            filePath.mkdirs();

        }
        Imgproc.cvtColor(pMat, pMat, Imgproc.COLOR_RGBA2BGR);
        final File file = new File(filePath, "QP_" + dateString + ".jpg");
        String filename = file.toString();
        Toast toast = Toast.makeText(getApplicationContext(), "Saved: " + filename + "\n to: "+ filePath, Toast.LENGTH_LONG);
        toast.show();
        return Highgui.imwrite(filename, pMat);


    }



    public Mat getQuantizisedImage(CameraBridgeViewBase.CvCameraViewFrame pInputFrame) {
        switch (quantizationMode) {
            /* Popup-Menü zur Auswahl des Quantisierungsmodus
            *   TODO: Quantisierungsmodi implementieren
            */
            case QUANT_MODE_0:
                //just return camera picture if no quantization is chosen
                return pInputFrame.rgba();

            case QUANT_MODE_1:
                // App stürtzt ab
            case QUANT_MODE_2:
                // App stürtzt ab

        }
        return pInputFrame.rgba();
    }


    private void createQuantizaionModeMenu(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] mode ={QUANT_MODE_0_STRING, QUANT_MODE_1_STRING, QUANT_MODE_2_STRING};
        builder.setTitle("Quantisierungsverfahren wählen:");
        builder.setSingleChoiceItems(mode, quantizationMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                quantizationMode = item;
            }
        });
        builder.show();
    }


}

