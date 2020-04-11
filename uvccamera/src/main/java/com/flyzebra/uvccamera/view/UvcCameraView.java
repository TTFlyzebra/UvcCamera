package com.flyzebra.uvccamera.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.flyzebra.debuglog.FlyLog;
import com.flyzebra.uvccamera.module.UvcCamera;
import com.flyzebra.uvccamera.receiver.UsbRecevier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-16-上午11:02.
 */

public class UvcCameraView extends TextureView {
    private UvcCamera uvcCamera;
    private final static Executor executor = Executors.newFixedThreadPool(1);
    private AtomicBoolean isShow = new AtomicBoolean(false);
    private Handler mHander = new Handler();
    private int width = 0;
    private int height = 0;
    private Surface mSurface;

    public UvcCameraView(Context context) {
        this(context, null);
    }

    public UvcCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UvcCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
//        getHolder().addCallback(callback);
        setSurfaceTextureListener(listener);
    }

    private void init(Context context) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        FlyLog.i("onMeasure");
        if (width == 0 || height == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(width, height);
        }
    }


    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurface = holder.getSurface();
            FlyLog.i("surfaceCreated");
            if (UsbRecevier.usbDevice == null) {
                List<UsbDevice> list = getDeviceList();
                if (list != null && list.size() > 0) {
                    UsbRecevier.usbDevice = getDeviceList().get(0);
                }
            }
            initUvcCamera(UsbRecevier.usbDevice);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            FlyLog.i("surfaceChanged width=%d,height=%d", width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            FlyLog.i("surfaceDestroyed");
            releaseCamera();
            isShow.set(false);
        }
    };

    private TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurface = new Surface(surface);
            FlyLog.i("onSurfaceTextureAvailable");
            if (UsbRecevier.usbDevice == null) {
                List<UsbDevice> list = getDeviceList();
                if (list != null && list.size() > 0) {
                    UsbRecevier.usbDevice = getDeviceList().get(0);
                }
            }
            initUvcCamera(UsbRecevier.usbDevice);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            FlyLog.i("onSurfaceTextureSizeChanged width=%d,height=%d", width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            FlyLog.i("onSurfaceTextureDestroyed");
            releaseCamera();
            isShow.set(false);
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            count++;
//            FlyLog.i("count = %d",count);
        }
    };


    private int count = 0;

    private Runnable resetTask = new Runnable() {
        @Override
        public void run() {
            if (count <= 5) {
                count = 0;
                FlyLog.i("run reset task count = %d", count);
                isShow.set(false);
                initUvcCamera(UsbRecevier.usbDevice);
            } else {
                count = 0;
                mHander.postDelayed(resetTask, 5000);
            }
        }
    };


    private void releaseCamera() {
        if (uvcCamera != null) {
            uvcCamera.destroy();
            uvcCamera = null;
        }
    }

    private Runnable initCameraTask = new Runnable() {
        @Override
        public void run() {
            try {
                releaseCamera();
                int result = 0;
                uvcCamera = new UvcCamera(getContext());
                result = uvcCamera.open(UsbRecevier.usbDevice);
                result = uvcCamera.setDefaultPreviewSize();
                result = uvcCamera.setDisplay(mSurface);
                result = uvcCamera.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void initUvcCamera(UsbDevice usbDevice) {
        if (usbDevice == null) {
            FlyLog.i("usbDevice = null!");
            return;
        }
//        mHander.postDelayed(resetTask, 5000);
        FlyLog.i("initUvcCamera");
        if (!isShow.get()) {
            isShow.set(true);
            executor.execute(initCameraTask);
        }
    }

    public void releaseUvcCamera(UsbDevice device) {
        releaseCamera();
    }

    @Override
    protected void onDetachedFromWindow() {
        mHander.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }

    public UvcCamera getCamera() {
        return uvcCamera;
    }

    public List<UsbDevice> getDeviceList() {
        UsbManager mUsbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        final List<UsbDevice> list = new ArrayList<UsbDevice>();
        list.addAll(deviceList.values());
        FlyLog.i("device list = "+list);
        return list;
    }
}
