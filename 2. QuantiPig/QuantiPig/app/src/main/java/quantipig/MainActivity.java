package quantipig;

// Importe für die verschiedenen Funktionalitäten.

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 *  Die Hauptklasse, die beim Start erstellt und initialisiert wird.
 *  Hier werden die Funktionen eingebunden und es wird auf bestimmte
 *  Aktivitäten des Nutzers reagiert.
 */
public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String QUANT_MODE_0_STRING = "Original";
    private static final String QUANT_MODE_1_STRING = "Pixel";
    private static final String QUANT_MODE_2_STRING = "Skalar";
    private static final int QUANT_MODE_0 = 0;
    private static final int QUANT_MODE_1 = 1;
    private static final int QUANT_MODE_2 = 2;
    public static int selectedCluster = 0;
    public static int cluster = 1;
    public static int modeSelector = 0;

    Button capture_button, quantimode_button, cluster_button;
    public static ProgressBar ladebalken;
    String fileName;
    Mat previewImage;

    final static String TAG = "QuantiPig";
    private int quantizationMode = QUANT_MODE_0;

    private CameraView cameraView;

    /* Das aktuell angezeigte Bild, das anschließend gespeichert werden kann. */
    private Mat currentInput;

    /*
     *   Initialisierung und Aktivierung des Frames zuständig. Den OpenCVManager einbinden.
     */
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
     * Intervallmenü für Modus Pixel
     */
    public static int setClusterPixel() {

              switch (selectedCluster) {
                    case 0: {
                        cluster = 1;
                        break;
                    }
                    case 1: {
                        cluster = 2;
                        break;
                    }
                    case 2: {
                        cluster = 4;
                        break;
                    }
                    case 3: {
                        cluster = 8;
                        break;
                    }
                    default: {
                        cluster = 1;
                        break;
                    }
                }
        return cluster;
    }
    /*
     * Intervallmenü für Modus Skalar
     */
    public static int setClusterSkalar(){

                switch (selectedCluster) {
                    case 0: {
                        cluster = 0;
                        break;
                    }
                    case 1: {
                        cluster = 1;
                        break;
                    }
                    case 2: {
                        cluster = 2;
                        break;
                    }
                    case 3: {
                        cluster = 3;
                        break;
                    }
                    case 4: {
                        cluster = 4;
                        break;
                    }
                    case 5: {
                        cluster = 5;
                        break;
                    }
                    case 6: {
                        cluster = 6;
                        break;
                    }
                    case 7: {
                        cluster = 7;
                        break;
                    }

                    default: {
                        cluster = 0;
                        break;
                    }
                }
        return cluster;
    }

    /*
     * Initialisierung wichtiger Elemente und setzen des Layouts mit Listenern.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        setContentView(quantipig.R.layout.activity_main);

        cameraView = (CameraView) findViewById(R.id.surface_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        ladebalken = (ProgressBar) findViewById(R.id.ladebalken);
        ladebalken.setVisibility(View.GONE);

        // Uri myUri = Uri.parse("file:///system/media/audio/ui/camera_click.ogg");
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.shuttersound);


        cluster_button = (Button) findViewById(R.id.button_cluster);
        cluster_button.setVisibility(View.GONE);
        cluster_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createClusterMenu();
            }
        });

        /* Button für die Quantisierungsstufen */

        quantimode_button = (Button) findViewById(R.id.button_quantimode);
        quantimode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setModus();
            }
        });

        /* Button zum Aufnehmen des Bildes */

        capture_button = (Button) findViewById(R.id.button_capture);
        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "playing Sound");
                mediaPlayer.start();
                ladebalken.setVisibility((View.VISIBLE));
                saveImage(previewImage);
                Log.d("Mat Click: ", "höhe: " + previewImage.height() + " breite: " + previewImage.width() + " channels: " + previewImage.channels());
            }
        });

        changePreviewSize();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        currentInput = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        currentInput.release();
    }

    /*
     * Das Live-Bild
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        currentInput = inputFrame.rgba();

        int mHeight  = inputFrame.rgba().height();                                          // Speichert die Höhe des Bildes
        int mWidth   = inputFrame.rgba().width();                                           // Speichert die Breite des Bildes
        int channels = inputFrame.rgba().channels();                                        // Speichert die Anzahl der Kanäle des Bildes

        switch (modeSelector) {                                                             // Abfrage des ausgewählten Modus
            case 0:                                                                         // Keine Quantisierung
                getsavingImage(inputFrame.rgba());
                break;

            case 1:                                                                         // Pixel
                cluster = setClusterPixel();                                                     // Abfrage des ausgewählten Intervalls
                currentInput = Pixel.pixel(inputFrame.rgba(), mHeight, mWidth, channels, cluster);
                getsavingImage(currentInput);
                break;


            case 2:
                cluster = setClusterSkalar();                                                     // Skalar
                currentInput = Skalar.skalar(currentInput, mHeight, mWidth, channels, cluster);
                getsavingImage(currentInput);
                break;

            default:
                return inputFrame.rgba();
        }
        return currentInput;
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
                        hideButton();
                        if(modeSelector==1 || modeSelector==2 ) {
                            modeSelector = 0;
                            changePreviewSize();  /* Verstecke Intervall-Button */
                        }
                        else
                            modeSelector = 0;
                        break;
                    }
                    case QUANT_MODE_1: {                                                            /* Pixel */
                        showButton();                                                               /* Zeige Intervall-Button */
                        if(modeSelector==0) {
                            modeSelector = 1;
                            changePreviewSize();
                        }
                        else
                            modeSelector = 1;

                        break;
                    }
                    case QUANT_MODE_2: {                                                            /* Skalar */
                        showButton();                                                               /* Zeige Intervall-Button */
                        if(modeSelector==0){
                            modeSelector = 2;
                            changePreviewSize();
                        }
                        else
                            modeSelector = 2;

                        break;
                    }
                }
                dialog.dismiss();                                                                   // Nach Auswahl wird das Menü geschlossen
            }
        });
        builder.show();
    }

    /* Erstellen des Dateinamens für das aufgenommene Biild. */

    public void saveImage(Mat mat) {
        Log.d("Mat saving:", "höhe: " + mat.height() + " breite: " + mat.width() + " channels: " + mat.channels() + " Type" + mat.type());
        SimpleDateFormat sdf = new SimpleDateFormat("yy_MM_dd-HH_mm_ss");
        String currentDateAndTime = sdf.format(new Date());
        Log.d("Mat saved:", "höhe: " + mat.height() + " breite: " + mat.width() + " channels: " + mat.channels() + " Type" + mat.type());
        switch (modeSelector) {
            case 0:
                fileName = QUANT_MODE_0_STRING + "_" + currentDateAndTime + ".png";
                break;
            case 1:
                fileName = QUANT_MODE_1_STRING + "-" + cluster + "_" + currentDateAndTime + ".png";
                break;
            case 2:
                fileName = QUANT_MODE_2_STRING + "-" + cluster + "_" + currentDateAndTime + ".png";
                break;
          }

        cameraView.takePicture(fileName);

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

    /* Intervallmenü */

    private void createClusterMenu() {
        AlertDialog.Builder clusterBuilder = new AlertDialog.Builder(this);
        final String[] clusterChoiceSkalar = {"0 x Bitshift", "1 x Bitshift", "2 x Bitshift", "3 x Bitshift", "4 x Bitshift", "5 x Bitshift", "6 x Bitshift", "7 x Bitshift"};
        final String[] clusterChoicePixel = {"1 x 1 Pixel", "2 x 2 Pixel", "4 x 4 Pixel", "8 x 8 Pixel"};
        if(modeSelector == 1) {
            clusterBuilder.setTitle("Quadratgröße festlegen");
            clusterBuilder.setSingleChoiceItems(clusterChoicePixel, selectedCluster, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    selectedCluster = item;
                    dialog.dismiss();
                }
            });
        }
        else if(modeSelector == 2) {
            clusterBuilder.setTitle("Anzahl der Bitshift-Operationen festlegen:");
            clusterBuilder.setSingleChoiceItems(clusterChoiceSkalar, selectedCluster, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    selectedCluster = item;
                    dialog.dismiss();
                }
            });
        }
        clusterBuilder.show();
    }

    /* Zeige IntervallButton */

    public void showButton() {
        cluster_button.getHandler().post(new Runnable() {
            @Override
            public void run() {
                cluster_button.setVisibility(View.VISIBLE);
            }
        });
    }

    /* Verstecke IntervallButton */

    public void hideButton() {
        cluster_button.getHandler().post(new Runnable() {
            @Override
            public void run() {
                cluster_button.setVisibility(View.GONE);
            }
        });
    }

    /* Änderung der Maße für das Live-Bild auf dem Display */

    public void changePreviewSize(){
        cameraView.disableView();
        switch (modeSelector){
            case 0: cameraView.setMaxFrameSize(1280, 960); break;
            case 1: cameraView.setMaxFrameSize(640, 480); break;
            case 2: cameraView.setMaxFrameSize(640, 480); break;
        }
        cameraView.enableView();
        }
}
