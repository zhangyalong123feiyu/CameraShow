package com.lenovo.carcamerashow;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Camera2TextureView extends TextureView implements TextureView.SurfaceTextureListener, ImageReader.OnImageAvailableListener {

    private static final String TAG = "zyl";

    private Context mContext;

    private String mCameraId;
    private CameraCharacteristics mBackCameraCharacteristics;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CaptureRequest mPreviewRequest;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;

    private Surface mWorkingSurface;
    private ImageReader mImageReader;
    private SurfaceTexture mSurfaceTexture;

    private HandlerThread mBackgroundThread;
    public Handler mBackgroundHandler;

    private Size mPreviewSize;
    private Point previewViewSize;
    private int rotation = 1;

    public Camera2TextureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mContext = context;
        initializeCameraManager();
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Camera2TextureView.this.mCameraDevice = cameraDevice;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
        }
    };

    protected void prepareCameraOutputs() {
        //获取相机支持的流的参数的集合
        StreamConfigurationMap map = mBackCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //寻找一个 最合适的尺寸
        mPreviewSize = getBestSupportedSize(new ArrayList<Size>(Arrays.asList(map.getOutputSizes(SurfaceTexture.class))));
        Log.e("TAG", "--mPreviewSize.getWidth()" + mPreviewSize.getWidth() + "mPreviewSize.getHeight()" + mPreviewSize.getHeight());
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                ImageFormat.YUV_420_888, 2);
        mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);
    }

    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            prepareCameraOutputs();
            if (isAvailable()) {
                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } else {
                setSurfaceTextureListener(this);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    EncodePushLiveH264 encodePushLiveH264;

    public void startCapture(SocketLive.SocketCallback socketCallback) {

        encodePushLiveH264 = new EncodePushLiveH264(socketCallback, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Log.e("TAG", "preview widith is " + mPreviewSize.getWidth() + "preview height is " + mPreviewSize.getHeight());
    }

    public void closeCamera() {
        closePreviewSession();
        closeCameraDevice();
        closeImageReader();
        stopBackgroundThread();
    }

    public void onDestroy() {
        stopBackgroundThread();
    }

    private void createCaptureSession() {
        try {

            mSurfaceTexture = getSurfaceTexture();

            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            mWorkingSurface = new Surface(mSurfaceTexture);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mWorkingSurface);
            mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

            mCameraDevice.createCaptureSession(Arrays.asList(mWorkingSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            updatePreview(cameraCaptureSession);
                        }

                        @Override
                        public void onConfigureFailed(
                                CameraCaptureSession cameraCaptureSession) {
                            Log.d(TAG, "Fail while starting preview: "+cameraCaptureSession.toString());
                        }
                    }, null);

        } catch (Exception e) {
            Log.e(TAG, "Error while preparing surface for preview: ", e);
        }
    }

    //https://blog.csdn.net/afei__/article/details/86108482
    private void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (null == mCameraDevice) {
            return;
        }
        mCaptureSession = cameraCaptureSession;
        //设置自动对焦模式为连续自动对焦
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        //创建CaptureRequest
        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            //开始预览，由于我们现在需要对预览的图像数据做处理
            mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT > 17) {
            mBackgroundThread.quitSafely();
        } else mBackgroundThread.quit();

        try {
            mBackgroundThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "stopBackgroundThread: ", e);
        } finally {
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == this || null == mPreviewSize) {
            return;
        }
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate((90 * (rotation - 2)) % 360, centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        this.setTransform(matrix);
    }

    public void initializeCameraManager() {

        startBackgroundThread();

        mCameraManager = (CameraManager) this.mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取可用的相机列表
            String[] cameraIdList = mCameraManager.getCameraIdList();
            for (String cameraId : cameraIdList) {
                //获取该相机的CameraCharacteristics，它保存的相机相关的属性
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                //获取相机的方向
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                //如果是前置摄像头就continue，我们这里只用后置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                //保存cameraId
                this.mCameraId = cameraId;
                this.mBackCameraCharacteristics = cameraCharacteristics;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            try {
                mCaptureSession.abortCaptures();
            } catch (Exception ignore) {
            } finally {
                mCaptureSession = null;
            }
        }
    }

    private void releaseTexture() {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    private void closeImageReader() {
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void closeCameraDevice() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        configureTransform(width,height);
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();
        image.close();
    }



    private Size getBestSupportedSize(List<Size> sizes) {
        Point maxPreviewSize = new Point(1920, 1080);
        Point minPreviewSize = new Point(1280, 720);
        Size defaultSize = sizes.get(0);
        Size[] tempSizes = sizes.toArray(new Size[0]);
        Arrays.sort(tempSizes, new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                if (o1.getWidth() > o2.getWidth()) {
                    return -1;
                } else if (o1.getWidth() == o2.getWidth()) {
                    return o1.getHeight() > o2.getHeight() ? -1 : 1;
                } else {
                    return 1;
                }
            }
        });
        sizes = new ArrayList<>(Arrays.asList(tempSizes));
        for (int i = sizes.size() - 1; i >= 0; i--) {
            if (maxPreviewSize != null) {
                if (sizes.get(i).getWidth() > maxPreviewSize.x || sizes.get(i).getHeight() > maxPreviewSize.y) {
                    sizes.remove(i);
                    continue;
                }
            }
            if (minPreviewSize != null) {
                if (sizes.get(i).getWidth() < minPreviewSize.x || sizes.get(i).getHeight() < minPreviewSize.y) {
                    sizes.remove(i);
                }
            }
        }
        if (sizes.size() == 0) {
            return defaultSize;
        }
        Size bestSize = sizes.get(0);
        float previewViewRatio;
        if (previewViewSize != null) {
            previewViewRatio = (float) previewViewSize.x / (float) previewViewSize.y;
        } else {
            previewViewRatio = (float) bestSize.getWidth() / (float) bestSize.getHeight();
        }

        if (previewViewRatio > 1) {
            previewViewRatio = 1 / previewViewRatio;
        }

        for (Size s : sizes) {
            if (Math.abs((s.getHeight() / (float) s.getWidth()) - previewViewRatio) < Math.abs(bestSize.getHeight() / (float) bestSize.getWidth() - previewViewRatio)) {
                bestSize = s;
            }
        }
        return bestSize;
    }
}
