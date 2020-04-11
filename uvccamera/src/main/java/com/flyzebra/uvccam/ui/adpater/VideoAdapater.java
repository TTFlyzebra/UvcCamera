package com.flyzebra.uvccam.ui.adpater;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flyzebra.utils.FlyLog;
import com.flyzebra.uvccam.R;
import com.flyzebra.uvccam.module.cache.DoubleBitmapCache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: FlyZebra
 * Created by flyzebra on 18-3-29-下午3:06.
 */

public class VideoAdapater extends RecyclerView.Adapter<VideoAdapater.ViewHolder> implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private List<String> mList;
    private Context mContext;
    private Set<GetUrlVideoBitmatTask> tasks = new HashSet<>();
    private DoubleBitmapCache doubleBitmapCache;

    public VideoAdapater(Context context, List<String> list, RecyclerView recyclerView) {
        mContext = context;
        mList = list;
        mRecyclerView = recyclerView;
        doubleBitmapCache = DoubleBitmapCache.getInstance(context.getApplicationContext());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case 0:
                        int first = ((GridLayoutManager) (mRecyclerView.getLayoutManager())).findFirstVisibleItemPosition();
                        int last = ((GridLayoutManager) (mRecyclerView.getLayoutManager())).findLastVisibleItemPosition();
                        GetBitmap(first, last);
                        break;
                    default:
                        cancleAllTask();
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    public void cancleAllTask() {
        for (GetUrlVideoBitmatTask task : tasks) {
            task.cancel(true);
        }
        tasks.clear();
    }

    private void GetBitmap(int first, int last) {
        for (int i = first; i <= last; i++) {
            Bitmap bitmap = doubleBitmapCache.get(mList.get(i));
            if (bitmap != null) {
                ImageView imageView = mRecyclerView.findViewWithTag(mList.get(i));
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                GetUrlVideoBitmatTask task = new GetUrlVideoBitmatTask(mList.get(i));
                task.execute(mList.get(i));
                tasks.add(task);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_01, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.iv01.setTag(mList.get(position));
        Bitmap bitmap = doubleBitmapCache.get(mList.get(position));
        if (bitmap != null) {
            holder.iv01.setImageBitmap(bitmap);
        } else {
            holder.iv01.setImageBitmap(null);
        }
        GetUrlVideoBitmatTask task = new GetUrlVideoBitmatTask(mList.get(position));
        task.execute(mList.get(position));
        tasks.add(task);
        holder.iv01.setOnClickListener(this);

    }


    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public void onClick(View v) {
        String path = (String) v.getTag();
        switch (v.getId()) {
            case R.id.itme_iv01:
                break;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv01;

        ViewHolder(View itemView) {
            super(itemView);
            iv01 = itemView.findViewById(R.id.itme_iv01);
        }
    }

    public class GetUrlVideoBitmatTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String url;

        GetUrlVideoBitmatTask(String url) {
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            try {
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(strings[0]);
                bitmap = media.getFrameAtTime(0);

                Matrix matrix = new Matrix();
                float scale = 320f/bitmap.getWidth();
                matrix.postScale(scale,scale);
                Bitmap bm = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

                bitmap = bm==null?bitmap:bm;
                final Bitmap bmp = bitmap;
                final String path = url;
                if (bitmap != null) {
                    publishProgress(bitmap);
                    if (doubleBitmapCache != null) {
                        doubleBitmapCache.put(path, bmp);
                    }
                }
                FlyLog.d("Get bitmap from http ok, url = %s, bitmap = " + bitmap, strings[0]);
            } catch (Exception e) {
                FlyLog.i("Get bitmap faile url = %s", strings[0]);
                e.printStackTrace();
            }
            FlyLog.d("bitmap=" + bitmap);
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            ImageView imageView = mRecyclerView.findViewWithTag(url);
            if (imageView != null) {
                imageView.setImageBitmap(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        }

    }

}
