package com.example.hadabele.quantipig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by hadabele on 29.12.2015.
 */
public class CameraView extends JavaCameraView implements Camera.PictureCallback{

    private static final String TAG = "QuantiPig - CameraView";
    private Mat mYuv = null;
    private String mPictureFileName;

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_SKALAR = 1;
    public static final int VIEW_MODE_MIDTREAD = 2;

    private static final int Cluster_0 = 0;
    public static final String Cluster_String_0 = "2 x 2";
    private static final int Cluster_1 = 1;
    public static  final String Cluster_String_1 = "4 x 4";
    private static final int Cluster_2 = 2;
    public static  final String Cluster_String_2 = "8 x 8";
    private static final int Cluster_3 = 3;
    public static String Cluster_String_3;

    private Mat mat;

    public static int mViewMode = VIEW_MODE_RGBA;

    private Context context;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }


    public void takePicture(final String fileName) {
        Log.i(TAG, "takePicture");
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

    public static void setViewModeRgba() {
        mViewMode = VIEW_MODE_RGBA;
    }

    public static void setViewModeSkalar() {
        mViewMode = VIEW_MODE_SKALAR;
    }

    public static void setViewModeMidtread() {
        mViewMode = VIEW_MODE_MIDTREAD;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Saving file");

        int mHeight;
        int mWidth;
        Mat mMat;
        byte[] buff;

        Camera.Parameters parameters = camera.getParameters();
        int mPictureFormat = parameters.getPictureFormat();

        switch (mPictureFormat) {
            case ImageFormat.JPEG:

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);

                if (mViewMode == VIEW_MODE_SKALAR || mViewMode == VIEW_MODE_MIDTREAD) {

                    mHeight = bmp.getHeight();
                    mWidth = bmp.getWidth();

                    mMat = new Mat(mHeight, mWidth, CvType.CV_8UC4);
                    Utils.bitmapToMat(bmp, mMat);
                    bmp = null;

                    buff = new byte[mWidth * mHeight * 4];
                    mMat.get(0, 0, buff);
                    mMat = null;

                    switch (mViewMode) {
                        case VIEW_MODE_RGBA:
                            buff = this.quantiMode0(buff, mHeight, mWidth);
                            break;

                        case VIEW_MODE_MIDTREAD:
                            buff = this.quantiMode2(buff, mHeight, mWidth);
                            break;

                        case VIEW_MODE_SKALAR:
                            buff = this.setCluster(buff, mHeight, mWidth);
                            break;
                    }

                    mMat = new Mat(mHeight, mWidth, CvType.CV_8UC4);
                    mMat.put(0, 0, buff);
                    buff = null;

                    bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mMat, bmp);
                    mMat.release();
                }

                try {
                    File rootPath;
                    switch (mViewMode) {
                        case VIEW_MODE_RGBA:
                            rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Original");
                            break;
                        case VIEW_MODE_SKALAR:
                            rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Skalar");
                            break;
                        case VIEW_MODE_MIDTREAD:
                            rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Midtread");
                            break;
                        default:
                            rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig");
                    }

                    if (!rootPath.exists()) {
                        rootPath.mkdirs();
                    }

                    FileOutputStream fileOutputStream = new FileOutputStream(mPictureFileName);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                }
                catch (IOException e) {
                    Log.e(TAG, "Fehler beim Speichern", e);
                }
                break;
        }
        Toast.makeText(context, "Bild gespeichert: " + mPictureFileName, Toast.LENGTH_LONG).show();
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera arg1) {
        MainActivity.ladebalken.setVisibility(View.GONE);
        Camera.Parameters parameters = arg1.getParameters();
        int format = parameters.getPreviewFormat();
         Camera.Size size = parameters.getPreviewSize();
        byte[] buff = new byte[0];
        mat = new Mat();
        switch (format) {
            case ImageFormat.JPEG:
                Log.d(TAG, "JPEG");
                break;
            case ImageFormat.NV16:
                Log.d(TAG, "NV16");
                break;
            //Standardformat unter Android und in OpenCV
            case ImageFormat.NV21:
                Log.d(TAG, "NV21");
                if (mYuv != null) mYuv.release();
                mYuv = new Mat(size.height + size.height / 2, size.width, CvType.CV_8UC1);
                mYuv.put(0, 0, frame);
                mat = new Mat();

                Imgproc.cvtColor(mYuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4);
                buff = new byte[size.width * size.height * 4];
                mat.get(0, 0, buff);

                break;

            // andere Formate werden nicht behandelt
            case ImageFormat.RAW10:
                Log.d(TAG, "RAW10");
                break;
            case ImageFormat.RAW_SENSOR:
                Log.d(TAG, "RAW_SENSOR");
                break;
            case ImageFormat.RGB_565:
                Log.d(TAG, "RGB_565");
                break;
            case ImageFormat.YUV_420_888:
                Log.d(TAG, "YUV_420_888");
                break;
            case ImageFormat.YUY2:
                Log.d(TAG, "YUY2");
                break;
            case ImageFormat.YV12:
                Log.d(TAG, "YV12");
                break;
            case ImageFormat.UNKNOWN:
                Log.d(TAG, "UNKNOWN");
                break;
        }

        switch (mViewMode) {
            case VIEW_MODE_RGBA:
                buff = this.quantiMode0(buff, size.height, size.width);
                break;

            case VIEW_MODE_MIDTREAD:
                buff = this.quantiMode2(buff, size.height, size.width);
                break;

            case VIEW_MODE_SKALAR:
                buff = this.setCluster(buff, size.height, size.width);
                break;
        }
        /**
         * Das modifizierte Array wird in ein Bitmap überführt und an das PreviewFrame übergeben
         */
        mat.put(0, 0, buff);
        View preview = findViewById(R.id.surface_view);
        Bitmap bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        preview.setBackgroundDrawable(new BitmapDrawable(bitmap));
        mat.release();

        super.onPreviewFrame(frame, arg1);
    }

    /**
     * Hier passiert nichts. Das Array wird einfach nur durhc die Methode durchgeschleust, ohne bearbeitet zu werden.
     * @param buff
     * @param mHeight
     * @param mWidth
     * @return buff
     */
    public static byte[] quantiMode0(byte[] buff, int mHeight, int mWidth) {
        return buff;
    }

    /**
     * Funktion ermittelt den größten gemeinsamen Teiler zweier Zahlen nach dem Euklidischen Algorithmus
     * @param zahl1
     * @param zahl2
     * @return größten gemeinsamen Teiler beider Zahlen
     */
    private static int ggT(int zahl1, int zahl2){
        while (zahl2 != 0) {
            if (zahl1 > zahl2) {
                zahl1 = zahl1 - zahl2;
            }
            else {
                zahl2 = zahl2 - zahl1;
            }
        }
        return zahl1;
    }

    /**
     * Tastet ein quadratisches Raster ab, ermittelt den den Durschnittlichen Wert der Kanäle und gibt diese für ddas Raster zurück
     * @param buff
     * @param mHeight
     * @param mWidth
     * @param cluster
     * @return modifiziertes Array mit den geänderten Farbwerten
     */
    public static byte[] quantiMode1(byte[] buff, int mHeight, int mWidth, int cluster) {

        int x = 0;                                                                                  // Index des abzutastenden Farbwertes aus dem Array buff[]
        int k = 0;                                                                                  // Index des Bildpunktes für das Hilfsarray
        int countY = 0;                                                                             // Variable, die die abgetasteten Zeilen zählt
        int[] frame = new int[mWidth / cluster * mHeight / cluster * 4];                            // Hilsarray zur Berechnung des Durchschnittwerts

        /**
         *  Befüllen des Hilsarrays frame
         *  mit:
         *  k = Bildpunktindex des Hilfarrays
         *  i = RGBA-Kanal
         *  x = Index des Originals
         */

        for (int n = 0; n < mHeight; n++) {                                                         // Erhöhung des Zeilenindexes
            countY++;                                                                               // Zähler für Zeilendurchläufe

            /* Innerhalb der Zeile */
            for (int m = 0; m < mWidth / cluster; m++) {                                            // Jede Zeile wird Breite:Cluster-mal durchlaufen
                for (int j = 0; j < cluster; j++) {                                                 // j -> Zählvariable in Abhängigkeit von cluster, wieviele Werte miteinander addiert werden müssen
                    for (int i = 0; i < 4; i++) {                                                   // i -> RGBA-Kanal (0=R, 1=G, 2=B, 3=A)
                        if (buff[x + i + j * 4] >= 0)                                               // Da signed Byte(-128 bis 127), muss geprüft werden ob der Wert mit negativem Vorueichen ist oder nicht
                            frame[k + i] += ((int) buff[x + i + j * 4]);                            // für >=0 kann der Wert einfach übernommen werden
                        else
                            frame[k + i] += ((int) buff[x + i + j * 4] & 0xff);                     // für < 0 wird der Wert umgerechnet, damit er positiv wird

                    }
                }
                k += 4;                                                                             // nächster Bildpunkt (+4, da RGBA)
                x += 4 * cluster;                                                                   // neuer abzutastender Wert, in Abhängigkeit vom Cluster
            }
            /* Sprung zur nächsten Zeile */
            if (countY < cluster)                                                                   // Zeilendurchläufe müssen gezählt werden
                k -= mWidth * 4 / cluster;                                                          // Wenn noch innerhalb des Clusters, muss k um eine Zeile zurückgesetzt werden
            else
                countY = 0;                                                                         // neuer Zählzyklus
        }


        /* Berechnen der Durchschnittswerte */

        for (int i = 0; i < frame.length; i++) {
            frame[i] = frame[i] / (cluster * cluster);                                              // Summme (frame[i]) : Anzahl der Elemente (cluster*cluster)
        }


        /**
         * Kopieren der Werte zurück in buff
         * Dabei werden die berechneten Int-Werte wieder zurück in Byte-Werte geschrieben
         */

        k = 0;                                                                                      // Rücksetzen der Variablen
        countY = 0;
        x = 0;

        for (int n = 0; n < mHeight; n++) {
            countY++;

            /* Innerhalb der Zeile */
            for (int m = 0; m < mWidth / cluster; m++) {
                for (int j = 0; j < cluster; j++) {
                    for (int i = 0; i < 4; i++) {
                        buff[x + i + j * 4] = (byte) frame[k + i];
                    }
                }
                k += 4;
                x += 4 * cluster;
            }
            /* Sprung zur nächsten Zeile */
            if (countY < cluster)
                k -= mWidth * 4 / cluster;
            else
                countY = 0;
        }

        return buff;
    }

    /**
     * Alle Werte werden mittels Shift-Funktionen bitweise manipuliert
     * @param buff
     * @param mHeight
     * @param mWidth
     * @return modifiziertes Array
     */
    public static byte[] quantiMode2(byte[] buff, int mHeight, int mWidth) {

        for (int i = 0; i < mWidth * mHeight * 4; i++) {
            buff[i] = (buff[i] >= 0) ? (byte) ((buff[i] >> 5) << 5) : (byte) (256 + (buff[i] >> 5) << 5);
        }
        return buff;
    }

    /**
     * Vorstude zu quantiMode1 - hier werdne zunächst die Cluster(Kantenlänge der Abtastquadrate)
     * aus der Auswahl im Kontextmenü(Intervall) übermittelt und in quantiMode1 übergeben.
     * @param buff
     * @param mHeight
     * @param mWidth
     * @return Array, das nach ausgewähltem Cluster modifiziert wurde
     */
    public static byte[] setCluster(byte[] buff, int mHeight, int mWidth) {

        int cluster = ggT(mHeight, mWidth);
        String ggT = new String (Integer.toString(cluster) + " x " + Integer.toString(cluster));
        Cluster_String_3 = ggT;
        /**
         *  Abfrage der Auswahl aus dem Intervall-Menü
         */
        switch (MainActivity.selectedCluster) {

            case Cluster_0: {
                quantiMode1(buff, mHeight, mWidth, 2);
                return buff;
            }

            case Cluster_1: {
                quantiMode1(buff, mHeight, mWidth, 4);
                return buff;
            }

            case Cluster_2: {
                quantiMode1(buff, mHeight, mWidth, 8);
                return buff;
                }

            case Cluster_3: {
                quantiMode1(buff, mHeight, mWidth, cluster);
                return buff;
            }

            default: {
                quantiMode1(buff, mHeight, mWidth, 2);
                return buff;
            }

        }
    }

}


