/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.flyzebra.uvccam.ui;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.flyzebra.debuglog.FlyLog;
import com.flyzebra.uvccam.R;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;
    private static final String TAG = "MainActivity";
    /**
     * set true if you want to record movie using MediaSurfaceEncoder
     * (writing frame data into Surface camera from MediaCodec
     * by almost same way as USBCameratest2)
     * set false if you want to record movie using MediaVideoEncoder
     */
    private static final boolean USE_SURFACE_ENCODER = false;
    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    private static final int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */
    private static final int PREVIEW_MODE = 0;

    /**
     * for accessing USB
     */
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera related methods sequentially on private thread
     */
    private UVCCameraHandler mCameraHandler;
    /**
     * for camera preview display
     */
    private CameraViewInterface mUVCCameraView;
    /**
     * for open&start / stop&close camera preview
     */
    private ToggleButton mCameraButton;
    /**
     * button for start/stop recording
     */
    private ImageButton mCaptureButton;

    private ImageButton mCaptureImage;
    private SeekBar sk01, sk02;
    private LinearLayout ll01;
    private Spinner sp01;
    private Button bt01;
    private ArrayAdapter<String> adapater;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable hidSetTask = new Runnable() {
        @Override
        public void run() {
            if (ll01 != null) {
                ll01.setVisibility(View.GONE);
            }
        }
    };
    private List<String> list = new ArrayList<>();
    private UVCCameraTextureView uvcCameraView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraButton = findViewById(R.id.camera_button);
        mCaptureButton = findViewById(R.id.capture_button);
        mCaptureImage = findViewById(R.id.capture_image);

        uvcCameraView = findViewById(R.id.camera_view);
        mUVCCameraView = (CameraViewInterface) uvcCameraView;
        mUVCCameraView.setAspectRatio(PREVIEW_WIDTH / (float) PREVIEW_HEIGHT);

        sk01 = findViewById(R.id.seekbar_brightness);
        sk02 = findViewById(R.id.seekbar_contrast);

        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mCaptureButton.setOnClickListener(mOnClickListener);
        mCaptureImage.setOnClickListener(mOnClickListener);


        ll01 = findViewById(R.id.settings);
        sp01 = findViewById(R.id.sp_pixels);
        bt01 = findViewById(R.id.browsefile);
        bt01.setOnClickListener(mOnClickListener);

        list.add(PREVIEW_WIDTH + "X" + PREVIEW_HEIGHT + (PREVIEW_MODE == 0 ? "-YUYV" : "-MJPEG"));
        adapater = new ArrayAdapter<>(this, R.layout.item_spinner, list);
        sp01.setAdapter(adapater);

        sk01.setMax(100);
        sk01.setProgress(50);
        sk02.setMax(100);
        sk02.setProgress(50);

        uvcCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacksAndMessages(null);
                setSeekBar();
                if (ll01.getVisibility() == View.VISIBLE) {
                    ll01.setVisibility(View.GONE);
                } else {
                    ll01.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(hidSetTask, 5000);
                }
            }
        });

        sk01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                FlyLog.i("can set brightness" + (isActive() && checkSupportFlag(UVCCamera.PU_BRIGHTNESS)));
                if (isActive() && checkSupportFlag(UVCCamera.PU_BRIGHTNESS)) {
                    FlyLog.i("setprogress brightness %d", progress);
                    setValue(UVCCamera.PU_BRIGHTNESS, progress);
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
                FlyLog.i("can set contrast" + (isActive() && checkSupportFlag(UVCCamera.PU_CONTRAST)));
                if (isActive() && checkSupportFlag(UVCCamera.PU_CONTRAST)) {
                    FlyLog.i("setprogress contrast %d", progress);
                    setValue(UVCCamera.PU_CONTRAST, progress);
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

    private void setSeekBar() {
        if (isActive()) {
            checkSupportFlag(UVCCamera.PU_BRIGHTNESS);
            sk01.setProgress(getValue(UVCCamera.PU_BRIGHTNESS));
            checkSupportFlag(UVCCamera.PU_CONTRAST);
            sk02.setProgress(getValue(UVCCamera.PU_CONTRAST));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView, USE_SURFACE_ENCODER ? 0 : 1,
                PREVIEW_WIDTH, PREVIEW_HEIGHT, PREVIEW_MODE);

        mUSBMonitor.register();
        mHandler.postDelayed(hidSetTask, 5000);
    }

    @Override
    protected void onStop() {
        if (mCameraHandler != null) {
            mCameraHandler.close();
            mCameraHandler.release();
            mCameraHandler = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        setCameraButton(false);
        mHandler.removeCallbacksAndMessages(null);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * event handler when click camera / capture button
     */
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.capture_button:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                            if (!mCameraHandler.isRecording()) {
                                mCaptureButton.setImageResource(R.drawable.recording_on);    // turn red
                                mCameraHandler.startRecording();
                            } else {
                                mCaptureButton.setImageResource(R.drawable.record_on);    // return to default color
                                mCameraHandler.stopRecording();
                            }
                        }
                    }
                    break;
                case R.id.capture_image:
                    if (mCameraHandler.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mCameraHandler.captureStill();
                        }
                    }
                    break;
                case R.id.browsefile:
                    startActivity(new Intent(MainActivity.this, FileActivity.class));
                    break;
            }
        }
    };

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
            switch (compoundButton.getId()) {
                case R.id.camera_button:
                    if (isChecked && !mCameraHandler.isOpened()) {
                        CameraDialog.showDialog(MainActivity.this);
                    } else {
                        mCameraHandler.close();
                        setCameraButton(false);
                    }
                    break;
            }
        }
    };

    private void setCameraButton(final boolean isOn) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraButton != null) {
                    try {
                        mCameraButton.setOnCheckedChangeListener(null);
                        mCameraButton.setChecked(isOn);
                    } finally {
                        mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
                    }
                }
            }
        }, 0);
    }

    private void startPreview() {
        final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
        mCameraHandler.startPreview(new Surface(st));
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            if (mCameraHandler != null) {
                mCameraHandler.close();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device != null && mUSBMonitor != null && device.getDeviceId() != 1004) {
                        mUSBMonitor.requestPermission(device);
                        setCameraButton(true);
                    }
                }
            }, 0);

        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            mCameraHandler.open(ctrlBlock);
            startPreview();
            setSeekBar();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            if (mCameraHandler != null) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mCameraHandler.close();
                    }
                }, 0);
                setCameraButton(false);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
            if (mCameraHandler != null) {
                mCameraHandler.close();
            }
        }

        @Override
        public void onCancel(final UsbDevice device) {
            setCameraButton(false);
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (DEBUG) Log.v(TAG, "onDialogResult:canceled=" + canceled);
        if (canceled) {
            setCameraButton(false);
        }
    }

    //================================================================================
    private boolean isActive() {
        return mCameraHandler != null && mCameraHandler.isOpened();
    }

    private boolean checkSupportFlag(final int flag) {
        return mCameraHandler != null && mCameraHandler.checkSupportFlag(flag);
    }

    private int getValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.getValue(flag) : 0;
    }

    private int setValue(final int flag, final int value) {
        return mCameraHandler != null ? mCameraHandler.setValue(flag, value) : 0;
    }

    private int resetValue(final int flag) {
        return mCameraHandler != null ? mCameraHandler.resetValue(flag) : 0;
    }


    private int mSettingMode = -1;
}
