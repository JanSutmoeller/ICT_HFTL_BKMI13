package com.example.hadabele.quantipig;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.opencv.*;


public class MainActivity extends Activity {

    private final static String TAG = "QuantiPig";
    private Size mPreviewSize;

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;

   private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mTextureView = (TextureView) findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mTextureView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "Button geklickt");
                takePicture();
            }
        });

    }

    protected void takePicture() {
        Log.e(TAG, "takePicture");
        if (null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Orientation

            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            // Timestamp für Dateinamen
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH_mm_ss");
            String date = sdf.format(c.getTime());

            //  Speichern des Bildes unter DCIM/QuantiPig
            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/QuantiPig");
            if(!filePath.exists()){
                filePath.mkdirs();
            }
            final File file = new File(filePath, "QuantiPig_" + date + ".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroundHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(MainActivity.this, "Gespeichert: " + file, Toast.LENGTH_SHORT).show();
                    startPreview();
                }
            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable, width=" + width + ", height=" + height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.e(TAG, "onSurfaceTextureUpdated");
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {

            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "onError");
        }
    };

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    protected void startPreview() {
        int rotationDegrees = 0;
        int displayRotation = getWindowManager().getDefaultDisplay().getRotation();
        Configuration config = getResources().getConfiguration();

        // Auslesen der Drehung
        switch (displayRotation) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = 270;
                break;
        }

        int deviceOrientation = Configuration.ORIENTATION_PORTRAIT;
        if ((rotationDegrees % 180 == 0 &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE) ||
                ((rotationDegrees % 180 != 0 &&
                        config.orientation == Configuration.ORIENTATION_PORTRAIT))) {
            deviceOrientation = Configuration.ORIENTATION_LANDSCAPE;
        }

        int effectiveWidth = mPreviewSize.getWidth();
        int effectiveHeight = mPreviewSize.getHeight();
        if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
            effectiveWidth = mPreviewSize.getHeight();
            effectiveHeight = mPreviewSize.getWidth();
        }



        // Find and center view rect and buffer rect
        Matrix transformMatrix = mTextureView.getTransform(null);
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufRect = new RectF(0, 0, effectiveWidth, effectiveHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        bufRect.offset(centerX - bufRect.centerX(), centerY - bufRect.centerY());

        // Undo ScaleToFit.FILL done by the surface
        transformMatrix.setRectToRect(viewRect, bufRect, Matrix.ScaleToFit.FILL);

        // Rotate buffer contents to proper orientation
        transformMatrix.postRotate((360 - rotationDegrees) % 360, centerX, centerY);
        if ((rotationDegrees % 180) == 90) {
            int temp = effectiveWidth;
            effectiveWidth = effectiveHeight;
            effectiveHeight = temp;
        }
        // Scale to fit view, cropping the longest dimension
        float scale =
                Math.max(viewWidth / (float) effectiveWidth, viewHeight / (float) effectiveHeight);
        transformMatrix.postScale(scale, scale, centerX, centerY);
        Handler handler = new Handler(Looper.getMainLooper());
        class TransformUpdater implements Runnable {
            TextureView mView;
            Matrix mTransformMatrix;

            TransformUpdater(TextureView view, Matrix matrix) {
                mView = view;
                mTransformMatrix = matrix;
            }

            @Override
            public void run() {
                mView.setTransform(mTransformMatrix);
            }
        }
        handler.post(new TransformUpdater(mTextureView, transformMatrix));



        if (mCameraDevice == null || !mTextureView.isAvailable() || mPreviewSize == null) {
            Log.e(TAG, "startPreview fail, return");
            return;
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (texture == null) {
            Log.e(TAG, "texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(effectiveWidth, effectiveHeight);
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(MainActivity.this, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
