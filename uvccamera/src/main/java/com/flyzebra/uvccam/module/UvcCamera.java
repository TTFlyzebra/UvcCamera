package com.flyzebra.uvccam.module;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;
import android.view.Surface;

import com.flyzebra.utils.FlyLog;
import com.flyzebra.uvccam.data.SuperSizeMode;
import com.serenegiant.usb.UVCCamera;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-16-下午2:27.
 */

public class UvcCamera {
    private UVCCamera mUVCCamer = new UVCCamera();
    private Context mContext;
    private long mNativePtr;
    private String mSupportedSize;
    private List<SuperSizeMode> sizeList;
    public int width = 800;
    public int height = 480;
    public int type = 1;

    private static final Executor executor = Executors.newFixedThreadPool(1);

    public UvcCamera(Context context) {
        mContext = context;
        mNativePtr = mUVCCamer.nativeCreate();
    }

    public int open(UsbDevice usbDevice) {
        int result;
        try {
            UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            if (usbManager == null) {
                return -1;
            }

            final String name = usbDevice.getDeviceName();
            final String[] v = !TextUtils.isEmpty(name) ? name.split("/") : null;
            int busnum = 0;
            int devnum = 0;
            if (v != null) {
                busnum = Integer.parseInt(v[v.length - 2]);
                devnum = Integer.parseInt(v[v.length - 1]);
            }

            String usbfsname = null;

            if ((v != null) && (v.length > 2)) {
                final StringBuilder sb = new StringBuilder(v[0]);
                for (int i = 1; i < v.length - 2; i++)
                    sb.append("/").append(v[i]);
                usbfsname = sb.toString();
            }

            if (TextUtils.isEmpty(usbfsname)) {
                FlyLog.i("failed to get USBFS path, try to use default path:" + name);
                usbfsname = "/dev/bus/usb";
            }

            int venderId = usbDevice.getVendorId();
            int productId = usbDevice.getProductId();
            int fileDescriptor = usbManager.openDevice(usbDevice).getFileDescriptor();
            FlyLog.i("nativeConnect id_camera=%d,venderId=%d,productId=%d,fileDescriptor=%d,busNum=%d,devAddr=%d,usbfs=%s",
                    mNativePtr, venderId, productId, fileDescriptor, busnum, devnum, usbfsname
            );

            result = mUVCCamer.nativeConnect(mNativePtr,
                    venderId,
                    productId,
                    fileDescriptor,
                    busnum,
                    devnum,
                    usbfsname);

            FlyLog.i("nativeConnect result=%d", result);

        } catch (final Exception e) {
            result = -1;
            FlyLog.i(e.toString());
        }
        if (result != 0) {
            FlyLog.i("open failed:result=%d", result);
            throw new UnsupportedOperationException("open failed:result=" + result);
        }
        return result;
    }


    public int setDefaultPreviewSize() {
        int result = 0;
        if (mNativePtr != 0 && TextUtils.isEmpty(mSupportedSize)) {
            mSupportedSize = UVCCamera.nativeGetSupportedSize(mNativePtr);
            FlyLog.i("nativeGetSupportedSize=%s", mSupportedSize);
            sizeList = getSupportedSize(mSupportedSize);
            FlyLog.i("sizelist=" + sizeList);
            if (!sizeList.isEmpty()) {
                width = sizeList.get(0).getWidth();
                height = sizeList.get(0).getHeight();
                type = sizeList.get(0).getType() == 4 ? 0 : 1;
                FlyLog.i("set size:%d*%d, %d", width, height, type);
            } else {
                FlyLog.i("getSupportedSize failed, set size: 848*480 ,1");
            }
            result = setPreviewSize(width, height, type);
        }
        return result;
    }

    public int setPreviewSize(int width, int height, int mode) {
        int result = -1;
        result = UVCCamera.nativeSetPreviewSize(mNativePtr, width, height, 1, 31, mode, 1.0f);
        FlyLog.i("nativeSetPreviewSize:result=%d", result);
        return result;
    }

    public int setDisplay(Surface mSurface) {
        int result = UVCCamera.nativeSetPreviewDisplay(mNativePtr, mSurface);
        FlyLog.i("nativeSetPreviewDisplay:result=%d", result);
        return result;
    }

    public int start() {
        int result = UVCCamera.nativeStartPreview(mNativePtr);
        FlyLog.i("nativeStartPreview:result=%d", result);
        mUVCCamer.updateCameraParams();
        return result;
    }

    public int stop() {
        return UVCCamera.nativeStopPreview(mNativePtr);
    }

    public void destroy() {
        stop();
        if (mNativePtr != 0) {
            UVCCamera.nativeRelease(mNativePtr);
        }
        if (mNativePtr != 0) {
            mUVCCamer.nativeDestroy(mNativePtr);
            mNativePtr = 0;
        }
    }

    private List<SuperSizeMode> getSupportedSize(String supportedSize) {
        final List<SuperSizeMode> list = new ArrayList<SuperSizeMode>();
        if (!TextUtils.isEmpty(supportedSize))
            try {
                final JSONObject json = new JSONObject(supportedSize);
                final JSONArray formats = json.getJSONArray("formats");
                final int format_nums = formats.length();
                for (int i = 0; i < format_nums; i++) {
                    final JSONObject format = formats.getJSONObject(i);
                    final int type = format.getInt("type");
                    if (format.has("type") && format.has("size")) {
                        final JSONArray size = format.getJSONArray("size");
                        final int size_nums = size.length();
                        for (int j = 0; j < size_nums; j++) {
                            try {
                                SuperSizeMode sizeMode = new SuperSizeMode();
                                sizeMode.setType(type);
                                final String[] sz = size.getString(j).split("x");
                                sizeMode.setWidth(Integer.parseInt(sz[0]));
                                sizeMode.setHeight(Integer.parseInt(sz[1]));
                                list.add(sizeMode);
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        return list;
    }

    public void setBrightness(final int brightness) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mUVCCamer.setBrightness(brightness);
                FlyLog.i("setBrightness %d", brightness);
            }
        });

    }

    public void setContrast(final int contrast) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                mUVCCamer.setContrast(contrast);
                FlyLog.i("setBrightness %d", contrast);
            }
        });

    }

    public List<SuperSizeMode> getSizeList(){
        return sizeList;
    }
}
