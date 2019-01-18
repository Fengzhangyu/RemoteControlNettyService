package com.zeasn.remotecontrol.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StorageUtil {

	private static final String INSTALL_PLUGIN_NAME = "vtionRemotePlugin.apk";

	/**
	 * 安装插件
	 * 
	 * @param context
	 *            活动上下文
	 */
	public static void installPlugin(Context context) {
		if (retrieveApkFromAssets(context)) {

			try {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(
						Uri.fromFile(new File(context.getFilesDir().getPath()
								+ "/", INSTALL_PLUGIN_NAME)),
						"application/vnd.android.package-archive");
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static boolean retrieveApkFromAssets(Context ctx) {
		AssetManager assets = ctx.getAssets();
		try {
			InputStream stream = assets.open("RemoteControlPluginSigned.apk");
			if (stream == null) {
				return false;
			}

			byte[] buf = new byte[1024];
			int nrOfBytes = 0;
			FileOutputStream fileOutputStream = ctx.openFileOutput(
					INSTALL_PLUGIN_NAME, Context.MODE_WORLD_READABLE);

			while ((nrOfBytes = stream.read(buf)) != -1) {
				fileOutputStream.write(buf, 0, nrOfBytes);
				fileOutputStream.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean isPkgInstalled(Context context, String packageName) {

		if (packageName == null || "".equals(packageName))
			return false;
		android.content.pm.ApplicationInfo info = null;
		try {
			info = context.getPackageManager().getApplicationInfo(packageName,
					0);
			return info != null;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

}
