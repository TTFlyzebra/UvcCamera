package com.flyzebra.uvccam.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.flyzebra.utils.FlyLog;
import com.flyzebra.uvccam.ui.MainActivity;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-16-下午3:07.
 */

public class UsbRecevier extends BroadcastReceiver {
    public static UsbDevice usbDevice;
    public static final String ACTION_USB_DEVICE_PERMISSION = "com.flyzebra.usb.permission.flyzebra";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            FlyLog.i("ACTION_USB_DEVICE_ATTACHED device=" + device);
            UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            assert mUsbManager != null;
            if (mUsbManager.hasPermission(device)) {
                usbDevice = device;
                Intent startIntent = new Intent(context, MainActivity.class);
                startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.putExtra(UsbManager.EXTRA_DEVICE, device);
                context.startActivity(startIntent);
            } else {
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
                mUsbManager.requestPermission(device, mPermissionIntent);
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            FlyLog.i("ACTION_USB_DEVICE_DETACHED device=" + device);
            if (usbDevice != null && device != null && usbDevice.getDeviceName().equals(device.getDeviceName())) {
                usbDevice = null;
                FlyLog.i("set usbdevice null");
            }
        } else if (ACTION_USB_DEVICE_PERMISSION.equals(action)) {
            final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            FlyLog.i("ACTION_USB_DEVICE_PERMISSION device=" + device);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                usbDevice = device;
            }
        }
    }

    public void register(Context context) throws IllegalStateException {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(this, filter);
    }

    public void unregister(Context context) throws IllegalStateException {
        context.unregisterReceiver(this);
    }
}
