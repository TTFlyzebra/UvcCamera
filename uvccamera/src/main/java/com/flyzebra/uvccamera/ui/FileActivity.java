package com.flyzebra.uvccamera.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.flyzebra.uvccamera.R;
import com.flyzebra.uvccamera.ui.fragment.PhotoFragment;
import com.flyzebra.uvccamera.ui.fragment.VideoFragment;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-18-上午10:14.
 */

public class FileActivity extends AppCompatActivity implements View.OnClickListener{
    private FrameLayout fl01,fl02;
    private ImageView iv01,iv02;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        fl01 = findViewById(R.id.file_photo);
        fl02 = findViewById(R.id.file_video);
        iv01 = findViewById(R.id.file_photo_iv);
        iv02 = findViewById(R.id.file_video_iv);
        iv01.setOnClickListener(this);
        iv02.setOnClickListener(this);

        getFragmentManager().beginTransaction().replace(R.id.file_fm01,new PhotoFragment()).commit();
        fl01.setBackgroundResource(R.drawable.bk_layout1);
        fl02.setBackgroundResource(R.drawable.bk_layout2);
        iv01.setImageResource(R.drawable.camera_over);
        iv02.setImageResource(R.drawable.record);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.file_photo_iv:
                getFragmentManager().beginTransaction().replace(R.id.file_fm01,new PhotoFragment()).commit();
                fl01.setBackgroundResource(R.drawable.bk_layout1);
                fl02.setBackgroundResource(R.drawable.bk_layout2);
                iv01.setImageResource(R.drawable.camera_over);
                iv02.setImageResource(R.drawable.record);
                break;
            case R.id.file_video_iv:
                getFragmentManager().beginTransaction().replace(R.id.file_fm01,new VideoFragment()).commit();
                fl01.setBackgroundResource(R.drawable.bk_layout2);
                fl02.setBackgroundResource(R.drawable.bk_layout1);
                iv01.setImageResource(R.drawable.camera_normal);
                iv02.setImageResource(R.drawable.record_on);
                break;
        }
    }

}
