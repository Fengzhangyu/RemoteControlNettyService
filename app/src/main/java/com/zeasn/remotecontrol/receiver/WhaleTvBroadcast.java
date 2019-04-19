package com.zeasn.remotecontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zeasn.remotecontrol.service.RemoteControlService;

/**
 * Created by Devin.F on 2019/4/4.
 */
public class WhaleTvBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("zeasn_WhaleTvBroadcast", "=OPEN");
        if (action.equals("com.zeasn.whale.RemoteBroadcast")) {//msgError
            String msgError = intent.getStringExtra("msgError");
            RemoteControlService.sendMsgToClient(msgError);
        }
    }

}
