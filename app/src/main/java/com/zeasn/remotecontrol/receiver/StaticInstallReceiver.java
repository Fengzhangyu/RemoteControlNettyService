package com.zeasn.remotecontrol.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.zeasn.remotecontrol.MainActivity;
import com.zeasn.remotecontrol.event.EventSendController;
import com.zeasn.remotecontrol.utils.Const;
import com.zeasn.remotecontrol.utils.MLog;


/**
 * 应用安装广播类 监听系统的安装、卸载广播 静态注册
 */
public class StaticInstallReceiver extends BroadcastReceiver {

    public static final String PLUGIN_PKG = "cn.vtion.remotecontrolplugin.tv.vstoresubclient";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        String pkgName = parsePkg(intent.getData());
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING,
                false);
        if (pkgName.equals(ctx.getPackageName()))
            return;
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            MLog.d("系统======接收到安装完毕广播pkgName=" + pkgName);

            if (PLUGIN_PKG.equals(pkgName)) {
                MainActivity.updateInjectService(ctx, EventSendController.PLUGIN_MODEL);
            }

        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            MLog.d("系统======接收到卸载广播pkgName=" + pkgName);

            if (PLUGIN_PKG.equals(pkgName)) {
                MainActivity.updateInjectService(ctx, EventSendController.ADB_MODEL);
            }

        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            MLog.d("系统======接收到替换广播pkgName=" + pkgName);

            if (PLUGIN_PKG.equals(pkgName)) {
                MainActivity.updateInjectService(ctx, EventSendController.PLUGIN_MODEL);
            }

        }
    }

    private String parsePkg(Uri uri) {
        return uri.toString().replace("package:", "");
    }


}
