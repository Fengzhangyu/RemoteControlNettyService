package com.zeasn.remotecontrol;

import android.app.Application;

import com.zeasn.remotecontrol.utils.NetworkUtils;

/**
 * Created by Devin.F on 2019/1/25.
 */
public class CustomApplication extends Application {

    public static boolean isNetWorkConnect;

    @Override
    public void onCreate() {
        super.onCreate();
        isNetWorkConnect = NetworkUtils.isNetworkOpen(this);
    }
}
