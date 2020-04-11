package com.flyzebra.uvccam.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flyzebra.utils.FlyLog;
import com.flyzebra.uvccam.R;
import com.flyzebra.uvccam.ui.adpater.PhotoAdapater;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Author: FlyZebra
 * Created by flyzebra on 18-4-2-下午4:14.
 */

public class PhotoFragment extends Fragment {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(1);
    private String savePath = Environment.getExternalStorageDirectory() + "/flyzebra/uvc/" + Environment.DIRECTORY_PICTURES;

    public PhotoFragment() {
    }

    private RecyclerView rv01;
    private List<String> rvList = new ArrayList<>();
    private PhotoAdapater photoAdapater;
    private List<String> temList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        rv01 = view.findViewById(R.id.rv01);
        rv01.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        photoAdapater = new PhotoAdapater(getActivity(), rvList, rv01);
        rv01.setAdapter(photoAdapater);
    }

    @Override
    public void onStart() {
        super.onStart();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(savePath);
                if (file.exists()) {
                    String fileStrs[] = file.list();

                    FlyLog.i("file list:" + fileStrs);

                    if (fileStrs != null && fileStrs.length > 0) {
                        temList.clear();

                        for (String str : fileStrs) {
                            temList.add(savePath + File.separator + str);
                        }

                        mHandler.removeCallbacksAndMessages(null);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                rvList.clear();
                                rvList.addAll(temList);
                                FlyLog.i("uplist list=%s" + rvList.toString());
                                photoAdapater.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        photoAdapater.cancleAllTask();
        super.onStop();
    }
}
