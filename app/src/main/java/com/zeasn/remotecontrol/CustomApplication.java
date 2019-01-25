package com.zeasn.remotecontrol;

import android.app.Application;
import android.content.Context;

import com.zeasn.remotecontrol.utils.NetworkUtils;

/**
 * Created by Devin.F on 2019/1/25.
 */
public class CustomApplication extends Application {
    private static Context context;

    public static boolean isNetWorkConnect;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        isNetWorkConnect = NetworkUtils.isNetworkOpen(context);
    }

    public static Context getContext() {
        if (context == null) {
            synchronized (CustomApplication.class) {
                context = new CustomApplication();
            }
        }
        return context;
    }
}
