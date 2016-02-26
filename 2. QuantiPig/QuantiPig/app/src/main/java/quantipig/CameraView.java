package quantipig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

/**
 * Created by HaDaBeLe on 22.02.2016.
 */

public class CameraView extends JavaCameraView implements Camera.PictureCallback {

    private String mPictureFileName;
    private File rootPath;

    public CameraView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public void takePicture(final String fileName){
        this.mPictureFileName = fileName;
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null, null, this);
    }

      /* Methode zum Speichern des Bildes */

    public void onPictureTaken( byte[] data, Camera camera){

        BitmapFactory.Options opt=new BitmapFactory.Options();
        Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,opt);
        Mat mat = new Mat(mBitmap.getHeight(), mBitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(mBitmap, mat);

            switch (MainActivity.modeSelector) {
                case 0:

                    rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Original");
                    break;
                case 1:
                    mat = Pixel.pixel(mat, mat.height(), mat.width(), mat.channels(), MainActivity.cluster);
                    rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Pixel");
                    break;
                case 2:
                    mat = Skalar.skalar(mat, mat.height(), mat.width(), mat.channels(), MainActivity.cluster);
                    rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig/Skalar");
                    break;
                default:
                    rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/QuantiPig");
            }
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2BGRA, 4);
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            File file = new File(rootPath, mPictureFileName);
            Boolean bool = Highgui.imwrite(file.toString(), mat);
            MainActivity.ladebalken.setVisibility((View.GONE));
            if (bool == true)
                Toast.makeText(super.getContext().getApplicationContext(), "Bild gespeichert: " + rootPath + "/" + mPictureFileName, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(super.getContext().getApplicationContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show();


        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

    }

}
