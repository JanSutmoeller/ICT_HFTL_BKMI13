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
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity implements CvCameraViewListener2 {

    Button capture_button, quantimode_button, cluster_button;
    public byte cluster_0 = (byte)191; //0
    public byte cluster_1 = (byte)127; //63
    public byte cluster_2 = (byte)63; //127
    public byte cluster_3 = (byte)0; //191
    public int matHeight, matWidth;
    /**
     * ImageContainer von OpenCV
     * s. URL: http://docs.opencv.org/java/2.4.2/org/opencv/core/Mat.html
     */
    private Mat mMat;
    private int quantizationMode = 0;

    // native RGBA-Modus
    private static final int QUANT_MODE_0 = 0;
    private static final String QUANT_MODE_0_STRING = "RGBA";

    // Darstellung von Grauwerten (Standard-OpenCV-Methode)
    private static final int QUANT_MODE_1 = 1;
    private static final String QUANT_MODE_1_STRING = "Grau";

    // Modus 3
    private static final int QUANT_MODE_2 = 2;
    private static final String QUANT_MODE_2_STRING = "Modus 2";

    /**
     * Brauch ich das?
     * TODO: Prüfen, ob eine globale Modusvariable sinnvoll ist. (Vlt Speicherorte nach Modus benennen?)
      */
    public int modeSelector = 0;

    public static final String TAG = "MainActivity";

    /*
     * Standard Callback für OpenCV
     * s. URL: http://docs.opencv.org/2.4/doc/tutorials/introduction/android_binary_package/dev_with_OCV_on_Android.html#dev-with-ocv-on-android
     */

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
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
    public void onResume() {
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


        capture_button = (Button)findViewById(R.id.button_capture);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                takePicture(mMat);

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
        /**
         *          Hier kommen die Methoden(-aufrufe) zur Bildmanipulation rein, bevor das Bild an das Display gesendet wird.
         */
        mMat = getQuantizisedImage(inputFrame);
        return mMat;

    }

    @Override
    public void onStart(){
     super.onStart();
    }



    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    // Methode zum Speichern des Bildes

    public boolean takePicture(Mat pMat){
        /**
         * Hier wird das Bild gespeichert
         * TODO: Umwandeln in BMP, weil JPEG Mist?
         */

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

        if (modeSelector == 1){
            Imgproc.cvtColor(pMat, pMat, Imgproc.COLOR_GRAY2BGR);
        }
        else
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
                /**
                 *  keine Quantisierung
                 */
                modeSelector = 0;
                return pInputFrame.rgba();

            case QUANT_MODE_1:{
                /**
                 *  Grauwerte
                 */
                modeSelector = 1;
                return pInputFrame.gray();
            }
            case QUANT_MODE_2: {
                /**
                 *  MODUS 3
                 */
                modeSelector =2;
                Log.e(TAG, "Modus 3");
                Mat tempMat;
                tempMat = pInputFrame.rgba();                           //  Übergeben des Cameraframes in Hilfsmatrix Matrix
                matHeight = tempMat.height();                           //  Auslesen der Matrixhöhe = Zeilenanzahl
                matWidth = tempMat.width();                             //  Auslesen der Matrixbreite = Spaltenanzahl
                byte [] pixels = new byte[matHeight*matWidth*4];        //  Byte-Array, als Container für die RGBA-Matrix
                tempMat.get(0,0, pixels);                               //  Befüllen des Arrays mit der RGBA-Matrix
                tempMat = null;                                         //  die Hilsmatrix wird null gesetzt
                int pos=0;
                int t, i, j;
                for(i = 0;  i < matHeight; i++) {
                    for (j = 0; j < matWidth; j++, pos ++) {
                        t = pos*4;
                        for (int k =0; k<3; k++) {
                            if (pixels[t+k] >= cluster_3)
                                pixels[t+k] = cluster_3;
                            else if (pixels[t+k] >= cluster_2 && pixels[t+k] < cluster_3)
                                pixels[t+k] = cluster_2;
                            else if (pixels[t+k] >= pixels[t+k] && pixels[t+k] < cluster_2)
                                pixels[t+k] = cluster_1;
                            else pixels[t+k] = cluster_0;
                        }
                        pixels[t+3] = (byte)0;

                    }
                }
                mMat = new Mat (matHeight, matWidth, CvType.CV_8UC4);
                mMat.put(0, 0, pixels);
                return mMat;
            }
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

