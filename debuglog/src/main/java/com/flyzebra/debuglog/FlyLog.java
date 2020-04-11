package com.flyzebra.debuglog;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Author: FlyZebra
 * Created by FlyZebra on 2018/3/28-下午3:28.
 */
public class FlyLog {
    public static final String TAG = "com.flyzebra.log";
    public static String[] filter = {
    };
    private static View view;
    private static boolean bWLog;
    private static ListView listView;
    private static List<String> list;
    private static ArrayAdapter<String> adapter;


    public static void d() {
        String s = buildLogString("");
        Log.d(TAG, s);
//        wlog(s);
    }

    public static void d(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.d(TAG, s);
//        wlog(s);
    }

    public static void i() {
        String s = buildLogString("");
        Log.i(TAG, s);
//        wlog(s);
    }

    public static void i(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.i(TAG, s);
//        wlog(s);
    }


    public static void v() {
        String s = buildLogString("");
        Log.v(TAG, s);
//        wlog(s);
    }

    public static void v(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.v(TAG, s);
//        wlog(s);
    }


    public static void e() {
        String s = buildLogString("");
        Log.e(TAG, s);
//        wlog(s);
    }

    public static void e(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.e(TAG, s);
//        wlog(s);
    }

    public static void w() {
        String s = buildLogString("");
        Log.w(TAG, s);
//        wlog(s);
    }

    public static void w(String logString, Object... args) {
        for (String aFilter : filter) {
            if (logString.indexOf(aFilter) == 0) {
                return;
            }
        }
        String s = buildLogString(logString, args);
        Log.w(TAG, s);
//        wlog(s);
    }


    private static String buildLogString(String str, Object... args) {
        if (args.length > 0) {
            str = String.format(str, args);
        }
        //进程消息
        Thread thread = Thread.currentThread();

        //打印位置
        StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("[")
                .append(thread.getName())
                .append("][")
                .append(thread.getId())
                .append("](")
                .append(caller.getFileName())
                .append(":")
                .append(caller.getLineNumber())
                .append(")")
                .append(caller.getMethodName())
                .append("()")
                .append(">>")
                .append(str);
        return stringBuilder.toString();
    }

//    public static void openWLog(Context context) {
//        view = LayoutInflater.from(context).inflate(R.layout.window_listview, null);
//        if (view == null) return;
//        view.setFocusable(false);
//        listView = (ListView) view.findViewById(R.id.listview);
//        list = new ArrayList<>();
//        adapter = new ArrayAdapter<>(context, R.layout.item_log_text, list);
//        listView.setAdapter(adapter);
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        if (wm == null) {
//            listView = null;
//            list.clear();
//            list = null;
//            adapter = null;
//            view = null;
//            return;
//        }
//
//        LayoutParams lp = new LayoutParams(LayoutParams.TYPE_TOAST);
////        lp.type = LayoutParams.TYPE_SYSTEM_ALERT;
//        lp.format = PixelFormat.TRANSPARENT;
//        lp.flags = LayoutParams.FLAG_NOT_FOCUSABLE|LayoutParams.FLAG_NOT_TOUCH_MODAL|LayoutParams.FLAG_NOT_TOUCHABLE;
//        lp.gravity = Gravity.TOP | Gravity.LEFT;
//        lp.x = 0;
//        lp.y = 1200;
//        wm.addView(view, lp);
//        bWLog = true;
//    }
//
//    public static void closeWLog(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        if (wm != null && view != null) {
//            wm.removeView(view);
//            listView = null;
//            list.clear();
//            list = null;
//            adapter = null;
//            view = null;
//        }
//        bWLog = false;
//    }
//
//    private static void wlog(String s) {
//        if (bWLog && list != null && adapter != null && listView != null) {
//            list.add(s);
//            adapter.notifyDataSetChanged();
//            listView.setSelection(list.size());
//        }
//    }

}