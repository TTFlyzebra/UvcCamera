package com.flyzebra.uvccam.ui;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Toast;

import com.flyzebra.uvccam.R;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.SimpleUVCCameraTextureView;

public class SimpleActivity extends BaseActivity implements USBMonitor.OnDeviceConnectListener {

    private SimpleUVCCameraTextureView mUVCCameraView;
    private USBMonitor mUSBMonitor;
    private final Object mSync = new Object();
    private UVCCamera mUVCCamera;
    private Surface mPreviewSurface;
    private Toast mToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        mUVCCameraView = findViewById(R.id.uvc_camera_01);
        mUSBMonitor = new USBMonitor(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.startPreview();
            }
        }
    }

    @Override
    protected void onStop() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        synchronized (mSync) {
            releaseCamera();
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mUVCCameraView = null;
        super.onDestroy();
    }

    private synchronized void releaseCamera() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                try {
                    mUVCCamera.setStatusCallback(null);
                    mUVCCamera.setButtonCallback(null);
                    mUVCCamera.close();
                    mUVCCamera.destroy();
                } catch (final Exception e) {
                    //
                }
                mUVCCamera = null;
            }
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
        }
    }


    @Override
    public void onAttach(UsbDevice device) {
        Toast.makeText(SimpleActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        if(device!=null){
            mUSBMonitor.requestPermission(device);
        }
    }

    @Override
    public void onDettach(UsbDevice device) {
        releaseCamera();
        Toast.makeText(SimpleActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnect(UsbDevice device, final UsbControlBlock ctrlBlock, boolean createNew) {
        releaseCamera();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                final UVCCamera camera = new UVCCamera();
                camera.open(ctrlBlock);
                if (mPreviewSurface != null) {
                    mPreviewSurface.release();
                    mPreviewSurface = null;
                }
                try {
                    camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                } catch (final IllegalArgumentException e) {
                    // fallback to YUV mode
                    try {
                        camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                    } catch (final IllegalArgumentException e1) {
                        camera.destroy();
                        return;
                    }
                }
                final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                if (st != null) {
                    mPreviewSurface = new Surface(st);
                    camera.setPreviewDisplay(mPreviewSurface);
//						camera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565/*UVCCamera.PIXEL_FORMAT_NV21*/);
                    camera.startPreview();
                }
                synchronized (mSync) {
                    mUVCCamera = camera;
                }
            }
        }, 0);
    }

    @Override
    public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
        releaseCamera();
    }

    @Override
    public void onCancel(UsbDevice device) {
    }
}
