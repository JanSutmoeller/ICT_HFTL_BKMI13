package com.example.hadabele.quantipig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  Hauptactivity
 *  Diese wActivity wird als erstes geladen, wenn die App gestartet wird
 */

public class MainActivity extends Activity{

    Button capture_button, quantimode_button, cluster_button;
    public static ProgressBar ladebalken;
    TextView tv_channels;
    String fileName;
    /**
     * ImageContainer von OpenCV
     * s. URL: http://docs.opencv.org/java/2.4.2/org/opencv/core/Mat.html
     */
    private int quantizationMode = QUANT_MODE_0;
    public static int selectedCluster = 0;

    /* keine Quantisierung */
    private static final int QUANT_MODE_0 = 0;
    private static final String QUANT_MODE_0_STRING = "Keine Quantisierung";

    /* Skalare Quantisierung */
    private static final int QUANT_MODE_1 = 1;
    private static final String QUANT_MODE_1_STRING = "Skalare Quantisierung";
    /* Midtread */
    private static final int QUANT_MODE_2 = 2;
    private static final String QUANT_MODE_2_STRING = "Midtread";

    private CameraView mCameraView;
    public int modeSelector = 0;
    public static final String TAG = "QuantiPig";


    /**
     * Erstellen aller wichtiger Komponenten (Buttons, Ladebalken, Frame für die Anzeige)
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = (CameraView) findViewById(R.id.surface_view);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(new CameraListener());

        ladebalken = (ProgressBar)findViewById(R.id.ladebalken);

        cluster_button = (Button) findViewById(R.id.button_cluster);
        cluster_button.setVisibility(View.GONE);
        cluster_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createClusterMenu();
            }
        });

        capture_button = (Button) findViewById(R.id.button_capture);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ladebalken.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd|HH_mm_ss");
                String currentDateAndTime = sdf.format(new Date());

                switch(CameraView.mViewMode){
                    case CameraView.VIEW_MODE_RGBA:
                        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() +
                                "/QuantiPig/Original/Original_" + currentDateAndTime + ".jpg";
                        break;
                    case CameraView.VIEW_MODE_SKALAR:
                        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() +
                                "/QuantiPig/Skalar/Skalar_"+ currentDateAndTime + ".jpg";
                        break;
                    case CameraView.VIEW_MODE_MIDTREAD:
                        fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() +
                                "/QuantiPig/Midtread/Midtread_" + currentDateAndTime + ".jpg";
                        break;
                }

                mCameraView.takePicture(fileName);
                //ladebalken.setVisibility(View.GONE);
            }
        });

        quantimode_button = (Button) findViewById(R.id.button_quantimode);
        quantimode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuantizationModeMenu();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
    }

    /*
     * Standard Callback für OpenCV
     * s. URL: http://docs.opencv.org/2.4/doc/tutorials/introduction/android_binary_package/dev_with_OCV_on_Android.html#dev-with-ocv-on-android
     */

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void createQuantizationModeMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] mode = {QUANT_MODE_0_STRING, QUANT_MODE_1_STRING, QUANT_MODE_2_STRING};
        builder.setTitle("Quantisierungsverfahren wählen:");
        builder.setSingleChoiceItems(mode, quantizationMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                quantizationMode = item;

                switch (quantizationMode) {
            /* Popup-Menü zur Auswahl des Quantisierungsmodus
            *   TODO: Quantisierungsmodi implementieren
            */

                    case QUANT_MODE_0: {
                        hideButton();
                        modeSelector = 0;
                        CameraView.setViewModeRgba();
                        break;
                    }

                    case QUANT_MODE_1: {
                        showButton();
                        modeSelector = 1;
                        CameraView.setViewModeSkalar();
                        break;
                    }

                    case QUANT_MODE_2: {
                        hideButton();
                        modeSelector = 2;
                        CameraView.setViewModeMidtread();
                        break;
                    }


                }


                dialog.dismiss();
            }
        });
        builder.show();
    }


    private void createClusterMenu() {
        AlertDialog.Builder clusterBuilder = new AlertDialog.Builder(this);
        final String[] clusterChoice = {CameraView.Cluster_String_0, CameraView.Cluster_String_1, CameraView.Cluster_String_2, CameraView.Cluster_String_3};
        clusterBuilder.setTitle("Skalierungsintervall festlegen:");
        clusterBuilder.setSingleChoiceItems(clusterChoice, selectedCluster, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                selectedCluster = item;
                dialog.dismiss();
            }
        });
        clusterBuilder.show();
    }

    public void showButton() {
        cluster_button.getHandler().post(new Runnable() {
            @Override
            public void run() {
                cluster_button.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideButton() {
        cluster_button.getHandler().post(new Runnable() {
            @Override
            public void run() {
                cluster_button.setVisibility(View.GONE);
            }
        });
    }
}

