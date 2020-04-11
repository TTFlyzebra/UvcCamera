package com.flyzebra.uvccam.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.flyzebra.utils.FlyLog;
import com.flyzebra.uvccam.R;
import com.flyzebra.uvccam.data.SuperSizeMode;
import com.flyzebra.uvccam.module.UvcCamera;
import com.flyzebra.uvccam.receiver.UsbRecevier;
import com.flyzebra.uvccam.view.UvcCameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-16-下午3:18.
 */

public class UvcCameraActivity extends AppCompatActivity {
    public static final String ACTION_USB_DEVICE_PERMISSION = "com.flyzebra.usb.permission.flyzebra";
    private IntentFilter filter;
    private UvcCameraView uvcCameraView;
    private SeekBar sk01, sk02;
    private LinearLayout ll01;
    private Spinner sp01;
    private ArrayAdapter<String> adapater;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable hidSetTask = new Runnable() {
        @Override
        public void run() {
            if(ll01!=null){
                ll01.setVisibility(View.GONE);
            }
        }
    };
    private List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uvccamera);

        filter = new IntentFilter();
        filter.addAction(ACTION_USB_DEVICE_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        uvcCameraView = findViewById(R.id.uvccameraview01);
        sk01 = findViewById(R.id.seekbar_brightness);
        sk02 = findViewById(R.id.seekbar_contrast);

        ll01 = findViewById(R.id.settings);

        sp01 = findViewById(R.id.sp_pixels);
        list.add("848X480 MJPEG");
        adapater = new ArrayAdapter<>(this,R.layout.item_spinner,list);
        sp01.setAdapter(adapater);

        sk01.setMax(100);
        sk01.setProgress(50);
        sk02.setMax(100);
        sk02.setProgress(50);

        uvcCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacksAndMessages(null);
                if(ll01.getVisibility()==View.VISIBLE){
                    ll01.setVisibility(View.GONE);
                }else {
                    ll01.setVisibility(View.VISIBLE);
                    UvcCamera uvcCamera = uvcCameraView.getCamera();
                    if (uvcCamera != null) {
                        List<SuperSizeMode> sizelist = uvcCamera.getSizeList();
                        if(sizelist!=null&&!sizelist.isEmpty()){
                            list.clear();
                            for(int i=0;i<sizelist.size();i++){
                                String type = sizelist.get(i).getType()==4?" YUYV":" MJPEG";
                                String str = sizelist.get(i).getWidth()+"X"+sizelist.get(i).getHeight()+type;
                                list.add(str);
                            }
                            adapater.notifyDataSetChanged();
                        }
                    }
                    mHandler.postDelayed(hidSetTask, 5000);
                }
            }
        });

        sk01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                UvcCamera uvcCamera = uvcCameraView.getCamera();
                if (uvcCamera != null) {
                    uvcCamera.setBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sk02.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                UvcCamera uvcCamera = uvcCameraView.getCamera();
                if (uvcCamera != null) {
                    uvcCamera.setContrast(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private BroadcastReceiver usbRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(context, "ATTACHED", Toast.LENGTH_LONG).show();
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                FlyLog.i("ACTION_USB_DEVICE_ATTACHED device=" + device);
                UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                if (mUsbManager.hasPermission(device)) {
                    UsbRecevier.usbDevice = device;
                    if (uvcCameraView != null) {
                        uvcCameraView.initUvcCamera(device);
                    }
                } else {
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_DEVICE_PERMISSION), 0);
                    mUsbManager.requestPermission(device, mPermissionIntent);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Toast.makeText(context, "DETACHED", Toast.LENGTH_LONG).show();
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                FlyLog.i("ACTION_USB_DEVICE_DETACHED device=" + device);
                if (device != null) {
//                    UsbRecevier.usbDevice = null;
                    uvcCameraView.releaseUvcCamera(device);
                }
            } else if (ACTION_USB_DEVICE_PERMISSION.equals(action)) {
                Toast.makeText(context, "PERMISSION", Toast.LENGTH_LONG).show();
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                FlyLog.i("ACTION_USB_DEVICE_PERMISSION device=" + device);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (uvcCameraView != null) {
                        UsbRecevier.usbDevice = device;
                        uvcCameraView.initUvcCamera(device);
                    }
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        FlyLog.i("onStart!");
        registerReceiver(usbRecevier, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(usbRecevier);
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }
}
