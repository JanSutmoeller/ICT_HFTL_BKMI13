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
import android.widget.TextView;
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

    TextView tv;
    Button capture_button, quantimode_button, cluster_button;
    public byte cluster_0 = (byte)0; //191
    public byte cluster_1 = (byte)63; //127
    public byte cluster_2 = (byte)(127&0xff); //63
    public byte cluster_3 = (byte)(191&0xff); //0
    public int matHeight, matWidth, arrayLength;
    /**
     * ImageContainer von OpenCV
     * s. URL: http://docs.opencv.org/java/2.4.2/org/opencv/core/Mat.html
     */
    private Mat mMat;
    private int quantizationMode = 0;

    // native RGBA-Modus
    private static final int QUANT_MODE_0 = 0;
    private static final String QUANT_MODE_0_STRING = "Modus 0";

    // Darstellung von Grauwerten (Standard-OpenCV-Methode)
    private static final int QUANT_MODE_1 = 1;
    private static final String QUANT_MODE_1_STRING = "Modus 1";

    // Modus 3
    private static final int QUANT_MODE_2 = 2;
    private static final String QUANT_MODE_2_STRING = "Modus 2";

    // Modus 4
    private static final int QUANT_MODE_4 = 3;
    private static final String QUANT_MODE_3_STRING = "Modus 3";

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
        //mMat = getQuantizisedImage(inputFrame);

        mMat = inputFrame.rgba();                                   //  Übergeben des Cameraframes in Hilfsmatrix Matrix
        matHeight = mMat.height();                                  //  Auslesen der Matrixhöhe = Zeilenanzahl
        matWidth = mMat.width();                                    //  Auslesen der Matrixbreite = Spaltenanzahl
        byte [] pixels = new byte[matHeight * matWidth * 4];        //  Byte-Array, als Container für die RGBA-Matrix
        mMat.get(0, 0, pixels);                                     //  Befüllen des Arrays mit der RGBA-Matrix
        mMat = null;                                                //  die Hilsmatrix wird null gesetzt
        byte []mbuff = getQuantizisedImage(pixels, matHeight, matWidth);
        mMat = new Mat (matHeight, matWidth, CvType.CV_8UC4);       //  CV_8UC4
        mMat.put(0, 0, mbuff);
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

        Imgproc.cvtColor(pMat, pMat, Imgproc.COLOR_RGBA2BGR);
        final File file = new File(filePath, "QP_" + dateString + ".jpg");
        String filename = file.toString();
        Toast toast = Toast.makeText(getApplicationContext(), "Saved: " + filename + "\n to: " + filePath, Toast.LENGTH_LONG);
        toast.show();

        return Highgui.imwrite(filename, pMat);
    }


    private byte[] getQuantizisedImage(byte[] pixels, int mHeight, int mWidth) {

        switch (quantizationMode) {
            /* Popup-Menü zur Auswahl des Quantisierungsmodus
            *   TODO: Quantisierungsmodi implementieren
            */


            case QUANT_MODE_0: {
                modeSelector = 0;
                quantiMode1(pixels, mHeight, mWidth);
                return pixels;
            }

            case QUANT_MODE_1: {
                modeSelector = 1;
                quantiMode2(pixels, mHeight, mWidth);
                return pixels;
            }
            case QUANT_MODE_2: {
                modeSelector = 2;
                quantiMode3(pixels, mHeight, mWidth);
                return pixels;
            }

        }

        return pixels;
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

    private byte[] quantiMode1(byte[] buff, int mHeight, int mWidth){
        int i, t;
        for ( i = 0; i < mWidth*mHeight; i++){
                t = i * 4;
            for (int k=0; k<3; k++){
                buff[t+k] = (buff[t+k] >= 0) ? (byte)( (buff[t+k ] >> 5) << 5) : (byte)( 256 + (buff[t+k ] >> 5) << 5);
            }
            buff[t+3]=(byte)255;
        }
        return buff;
    }

    private byte[] quantiMode2 (byte[] buff, int mHeight, int mWidth){
        int cluster = 16;            // Kantenlänge des Quadrats, aus dem der Durchschnitt berechnet werden soll
        int x       = 0;
        int k       = 0;
        int countY  = 0;
        int [] frame = new int[mWidth/cluster*mHeight/cluster*4];       // Hilsframe zur Berechnung des Durchschnittwerts

        /**
         *  Befüllen des Hilsarrays frame
         *  mit:
         *  k = Bildpunktindex des Hildarrays
         *  i = RGBA-Kanal
         *  x = Index des Originals
         */

        for (int n = 0; n < mHeight; n++){                              // Erhöhung des Zeilenindexes
            countY ++;                                                  // Zähler für Zeilendurchläufe

            //Innerhalb der Zeile
            for (int m = 0; m < mWidth/cluster; m++){                   // Jede Zeile wird Breite:Cluster-mal durchlaufen
                for (int j = 0; j <  cluster; j++){                     // j -> Zählvariable in Abhängigkeit von cluster, wieviele Werte miteinander addiert werden müssen
                    for(int i = 0; i < 4; i++){                         // i -> RGTBA-Kanal (0=R, 1=G, 2=B, 3=A)
                        if(buff[x+i+j*4] >= 0)                          // Da signed Byte(-128 bis 127), muss geprüft werden ob der Wert mit negativem Vorueichen ist oder nicht
                            frame[k+i]+=((int)buff[x+i+j*4]);           // für >=0 kann der Wert einfach übernommen werden
                        else
                            frame[k+i]+=((int)buff[x+i+j*4]&0xff);      // für < 0 wird der Wert umgerechnet, damit er positiv wird
                    }
                }
                k+=4;                                                   //
                x+=4*cluster;                                           // x -> entspricht dem index des Original Arrays buff
            }
            //Zeile +=1
            if(countY < cluster)                                        // Zeilendurchläufe müssen gezählt werden
                k-=mWidth*4/cluster;                                    // Wenn noch innerhalb des Clusters, muss k um eine Zeile zurückgesetzt werden
            else
                countY = 0;                                             // neuer Zählzyklus
        }

        /**
         * Berechnen der Durchschnittswerte
         */

        for(int i = 0; i<frame.length; i++){
            frame[i] = frame[i] / (cluster*cluster);                    // Summme (frame[i] / Anzahl der Elemente (cluster*cluster)
        }


        /**
         * Kopieren der Werte zurück in buff
         *
         * dabei werden die berechneten Int-werte wieder zurück in Byte-Werte geschrieben
         */

        k=0;                                                            // Rücksetzen der Variablen
        countY=0;
        x=0;

        for (int n = 0; n < mHeight; n++){
            countY ++;

            //Innerhalb der Zeile
            for (int m = 0; m < mWidth/cluster; m++){
                for (int j = 0; j <  cluster; j++){
                    for(int i = 0; i < 4; i++){
                        buff[x+i+j*4] = (byte)frame[k+i];
                    }
                }
                k+=4;
                x+=4*cluster;
            }
            //Zeile +=1
            if(countY < cluster)
                k-=mWidth*4/cluster;
            else
                countY = 0;
        }

        return buff;
    }

    private byte[] quantiMode3 (byte[] buff, int mHeight, int mWidth){

        return buff;
    }
}

