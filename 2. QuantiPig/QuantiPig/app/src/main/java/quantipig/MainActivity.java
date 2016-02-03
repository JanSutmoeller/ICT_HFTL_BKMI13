package quantipig;

// Importe für die verschiedenen Funktionalitäten.

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
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

// Die Hauptklasse, die beim Start erstellt und initialisiert wird.
// Hier werden die Funktionen eingebunden und es wird auf bestimmte
// Aktivitäten des Nutzers reagiert.
public class MainActivity extends Activity {

    public static final String Cluster_String_0 = "Intervall = 1";
    public static final String Cluster_String_1 = "Intervall = 2";
    public static final String Cluster_String_2 = "Intervall = 4";
    public static final String Cluster_String_3 = "Intervall = 8";
    private static final int Cluster_0 = 0;
    private static final int Cluster_1 = 1;
    private static final int Cluster_2 = 2;
    private static final int Cluster_3 = 3;
    private static final String QUANT_MODE_0_STRING = "Original";
    private static final String QUANT_MODE_1_STRING = "Pixel";
    private static final String QUANT_MODE_2_STRING = "Skalar";
    private static final int QUANT_MODE_0 = 0;
    private static final int QUANT_MODE_1 = 1;
    private static final int QUANT_MODE_2 = 2;
    public static int selectedCluster = 0;
    public static int cluster = 1;
    public int modeSelector = 0;
    Button capture_button, quantimode_button, cluster_button;
    String fileName;
    Mat previewImage;
    //Ein Zeitstempel, um die Zeit des Haltens des Fingers auf dem Bildschirm zu messen.
    long time = 0;
    private int quantizationMode = QUANT_MODE_0;
    /*
    Globale Variablen für die App
    Das GUI-Element zur Anzeige der quantisierten Bilder.
    */
    private CameraBridgeViewBase cameraView;

    //Das aktuell angezeigte Bild, dass dann gespeichert werden kann.
    private Mat currentInput;

    //Initialisierung und Aktivierung des Frames zuständig. Den OpenCVManager einbinden.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    cameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    /*
    *  Abfrage der Auswahl aus dem Intervall-Menü
    */
    public static int setCluster() {
        switch (selectedCluster) {
            case Cluster_0: {
                cluster = 1;
                return cluster;
            }
            case Cluster_1: {
                cluster = 2;
                return cluster;
            }
            case Cluster_2: {
                cluster = 4;
                return cluster;
            }
            case Cluster_3: {
                cluster = 8;
                return cluster;
            }
            default: {
                cluster = 1;
                return cluster;
            }
        }
    }

    // Initialisierung wichtiger Elemente und setzen des Layouts mit Listenern.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(quantipig.R.layout.activity_main);
        cameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);

        cluster_button = (Button) findViewById(R.id.button_cluster);
        cluster_button.setVisibility(View.GONE);
        cluster_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createClusterMenu();
            }
        });
        quantimode_button = (Button) findViewById(R.id.button_quantimode);
        quantimode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setModus();
            }
        });

        capture_button = (Button) findViewById(R.id.button_capture);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveImage(previewImage);
                Log.d("Mat Click: ", "höhe: " + previewImage.height() + " breite: " + previewImage.width() + " channels: " + previewImage.channels());
            }
        });


        imageProcessing();
    }

    //Setzen der Listener und einbinden der Funktionen, der dann eintretenden Aktionen.
    public void imageProcessing() {
        cameraView.setVisibility(SurfaceView.VISIBLE);

        //Überschreiben des Frames mit dem ausgewählten Verfahren.
        cameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                Mat mMat;
                int mHeight  = inputFrame.rgba().height();                                          // Speichert die Höhe des Bildes
                int mWidth   = inputFrame.rgba().width();                                           // Speichert die Breite des Bildes
                int channels = inputFrame.rgba().channels();                                        // Speichert die Anzahl der Kanäle des Bildes

                switch (modeSelector) {                                                             // Abfrage des ausgewählten Modus
                    case 0:                                                                         // Keine Quantisierung
                        getsavingImage(inputFrame.rgba());
                        return inputFrame.rgba();

                    case 1:                                                                         // Pixel
                        cluster = setCluster();                                                     // Abfrage des ausgewählten Intervalls
                        mMat = Pixel.pixel(inputFrame.rgba(), mHeight, mWidth, channels, cluster);
                        getsavingImage(mMat);
                        return mMat;

                    case 2:
                        cluster = setCluster();                                                     // Skalar
                        mMat = Skalar.skalar(inputFrame.rgba(), mHeight, mWidth, channels, cluster);
                        getsavingImage(mMat);
                        return mMat;

                    default:
                        return inputFrame.rgba();
                }
            }
        });
    }

    public void getsavingImage(Mat mat) {
        previewImage = mat;
    }
    /*
     * Popup-Menü zur Auswahl des Quantisierungsmodus
     */
    private void setModus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] mode = {QUANT_MODE_0_STRING, QUANT_MODE_1_STRING, QUANT_MODE_2_STRING};
        builder.setTitle("Quantisierungsverfahren wählen:");
        builder.setSingleChoiceItems(mode, quantizationMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                quantizationMode = item;

                switch (quantizationMode) {                                                         /* Abfrage der Menüauswahl */
                    case QUANT_MODE_0: {                                                            /* Keine Quantisierung */
                        hideButton();                                                               /* Verstecke Intervall-Button */
                        modeSelector = 0;
                        break;
                    }
                    case QUANT_MODE_1: {                                                            /* Pixel */
                        showButton();                                                               /* Zeige Intervall-Button */
                        modeSelector = 1;
                        break;
                    }
                    case QUANT_MODE_2: {                                                            /* Skalar */
                        showButton();                                                               /* Zeige Intervall-Button */
                        modeSelector = 2;
                        break;
                    }
                }
                dialog.dismiss();                                                                   // Nach Auswahl wird das Menü geschlossen
            }
        });
        builder.show();
    }

    //Das Speichern des momentan angezeigten Bildes als PNG-File im Ordner QuantiPig unter DCIM.
    public void saveImage(Mat mat) {
        Log.d("Mat saving:", "höhe: " + mat.height() + " breite: " + mat.width() + " channels: " + mat.channels() + " Type" + mat.type());
        File rootPath;

        SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd-HH_mm_ss");
        String currentDateAndTime = sdf.format(new Date());
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGRA, 4);
        Log.d("Mat saved:", "höhe: " + mat.height() + " breite: " + mat.width() + " channels: " + mat.channels() + " Type" + mat.type());
        switch (modeSelector) {
            case 0:
                rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Original");
                fileName = QUANT_MODE_0_STRING + "_" + currentDateAndTime + ".png";
                break;
            case 1:
                rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Pixel");
                fileName = QUANT_MODE_1_STRING + "-" + cluster + "_" + currentDateAndTime + ".png";
                break;
            case 2:
                rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Skalar");
                fileName = QUANT_MODE_2_STRING + "-" + cluster + "_" + currentDateAndTime + ".png";
                break;
            default:
                rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig");
        }
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        File file = new File(rootPath, fileName);

        Boolean bool = Highgui.imwrite(file.toString(), mat);

        if (bool == true)
            Toast.makeText(getApplicationContext(), "Bild gespeichert: " + rootPath + "/" + fileName, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
        }

    //Override-Methoden, um die OneCvView zu schließen oder zu pausieren.
    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }

    //Laden des OpenCvManagers.
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void createClusterMenu() {
        AlertDialog.Builder clusterBuilder = new AlertDialog.Builder(this);
        final String[] clusterChoice = {Cluster_String_0, Cluster_String_1, Cluster_String_2, Cluster_String_3};
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