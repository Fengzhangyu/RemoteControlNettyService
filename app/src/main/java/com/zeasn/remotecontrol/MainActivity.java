package com.zeasn.remotecontrol;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zeasn.remotecontrol.utils.Const;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent();//context, RemoteControlService.class
        intent.setAction(Const.START_REMOTE_CONTROL_ACTION);
        intent.setPackage(getPackageName());
// 启动服务的地方
//        if (Build.VERSION.SDK_INT >= 26) {
//            context.startForegroundService(intent);
//        } else {
        // Pre-O behavior.
        startService(intent);

    }
}
