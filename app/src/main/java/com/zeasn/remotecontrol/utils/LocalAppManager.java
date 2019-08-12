package com.zeasn.remotecontrol.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zeasn.remotecontrol.CustomApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rico on 2018/1/1.
 */

public class LocalAppManager {

    public static LocalAppManager localAppManager;
    public static PackageManager packageManager;
    final String[] categoryList = {Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_LEANBACK_LAUNCHER};

    public static LocalAppManager getInitialize() {
        if (localAppManager == null) {
            synchronized (LocalAppManager.class) {
                if (localAppManager == null) {
                    packageManager = CustomApplication.getContext().getPackageManager();
                    localAppManager = new LocalAppManager();
                }
            }
        }
        return localAppManager;
    }

    public static PackageManager getPackageManager() {
        if (packageManager == null)
            packageManager = CustomApplication.getContext().getPackageManager();
        return packageManager;
    }


    public static class AppInfo implements Serializable {
        private String lable;
        private String pkg;
        private String apkPath;
        private boolean isSysApp;

        public String getLable() {
            return lable;
        }

        public void setLable(String lable) {
            this.lable = lable;
        }

        public String getPkg() {
            return pkg;
        }

        public void setPkg(String pkg) {
            this.pkg = pkg;
        }

        public boolean isSysApp() {
            return isSysApp;
        }

        public void setSysApp(boolean sysApp) {
            isSysApp = sysApp;
        }

        public JSONObject toJSONObject() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("lable", getLable());
                obj.put("pkg", getPkg());
//                obj.put("apkPath", getApkPath());
//                obj.put("isSysApp", isSysApp());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        public String getApkPath() {
            return apkPath;
        }

        public void setApkPath(String apkPath) {
            this.apkPath = apkPath;
        }
    }

    public static List<AppInfo> queryAppInfo(Context context, boolean containSysApp) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        for (ApplicationInfo app : listAppcations) {
            if (containSysApp || (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                boolean isSysApp = (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                //过滤掉系统底层的app
                if (isSysApp &&
                        (app.packageName.startsWith("com.android.") || app.packageName.equals("android")))
                    continue;
                AppInfo appInfo = new AppInfo();
                appInfo.setLable((String) app.loadLabel(pm));
                appInfo.setPkg(app.packageName);
                appInfo.setApkPath(app.sourceDir);
                appInfo.setSysApp(isSysApp);
                appInfos.add(appInfo);

//                RemoteControlService.sendMsgToClient(appInfo.toJSONObject().toString());

//                RemoteControlService.sendMsgToClient(new String(getAppIcon(app.packageName, context)));

//                String temp0 = new String(getAppIcon(app.packageName, context));
//                String temp1 = ZipUtils.gzip(new String(getAppIcon(app.packageName, context)));
//                String temp2 = ZipUtils.zip(new String(getAppIcon(app.packageName, context)));

//                RemoteControlService.sendMsgToClient(temp1);


            }
        }
        Collections.sort(appInfos, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                int i1 = (o1.isSysApp ? 2 : 1);
                int i2 = (o2.isSysApp ? 2 : 1);
                if (i1 == i2) {
                    return o1.getLable().compareTo(o2.getLable());
                } else {
                    return (i1 < i2) ? -1 : 1;
                }
            }
        });
        return appInfos;
    }

    public static byte[] getAppIcon(String packageName, Context context) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName, context);
        if (applicationInfo == null) return null;
        BitmapDrawable bitmap = (BitmapDrawable) applicationInfo.loadIcon(context.getPackageManager());

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        bitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, data);
        return data.toByteArray();
    }

    public static BitmapDrawable getAppIcon1(String packageName, Context context) {
        ApplicationInfo applicationInfo = getApplicationInfo(packageName, context);
        if (applicationInfo == null) return null;
        BitmapDrawable bitmap = (BitmapDrawable) applicationInfo.loadIcon(context.getPackageManager());

        ByteArrayOutputStream data = new ByteArrayOutputStream();
        bitmap.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, data);
        return bitmap;
    }

    private static ApplicationInfo getApplicationInfo(String packageName, Context context) {
        ApplicationInfo applicationInfo = null;
        if (!packageName.isEmpty()) {
            PackageManager pm = context.getPackageManager();
            try {
                applicationInfo = pm.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException ex) {
                applicationInfo = null;
            }
        }
        return applicationInfo;
    }

    public static String getQueryAppInfoJsonString(Context context, boolean containSysApp) {
        List<AppInfo> appInfos = queryAppInfo(context, containSysApp);
        JSONArray array = new JSONArray();
        for (AppInfo app : appInfos) {
            array.put(app.toJSONObject());
        }
        return array.toString();
    }




    /***
     * 本地是否安装
     * @param pkgName
     * @return
     */
    public static boolean isPkgInstalled(String pkgName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(pkgName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 启动第三方应用
     *
     * @param context
     * @param pkgName
     */
    public static void startNonPartyApplication(final Context context, final String pkgName) {
        try {

            String package_name = pkgName;
            PackageManager packageManager = context.getPackageManager();
            Intent it = packageManager.getLaunchIntentForPackage(package_name);
            Log.v("ExPackageManager", " ==== " + pkgName);
            if (null != it) {
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//可选
                Log.v("ExPackageManager", "packname open>>");
                context.startActivity(it);
            } else {
                Log.v("ExPackageManager", "packname add classname open>>");
                String activity_path = getClassName(pkgName);
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//可选
                ComponentName cn = new ComponentName(package_name, activity_path);
                intent.setComponent(cn);
                if (intent.resolveActivityInfo(getPackageManager(), PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    context.startActivity(intent);
                }
            }


        } catch (Exception ex) {

            ex.printStackTrace();
        }

    }

    public static String getClassName(String pkgName) {
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.setPackage(pkgName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        Log.v("ExPackageManager", "resolveinfoList size>>" + resolveinfoList.size());
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            if (!TextUtils.isEmpty(className))
                return className;
            else
                return "";
        }
        return "";
    }


    /**
     * 启动第三方应用
     *
     * @param context
     * @param pkgName
     */
    public static void startUriNonPartyApplication(final Context context, final View view, final String pkgName) {
        try {
            Uri uri = Uri.parse("market://details?id=" + pkgName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setPackage(APP_STORE_PKG);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Please install APP Store", Toast.LENGTH_SHORT).show();
        }
    }

    /* 卸载apk */
    public static void uninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        context.startActivity(intent);
    }


    public static boolean isExistPkg(String packageName) {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /***
     * 是否为系统应用
     * @param pkgName
     * @return
     */
    public static boolean isSystemApp(String pkgName) {
        boolean isSystemApp = false;
        try {
            PackageInfo mPackageInfo = getPackageManager().getPackageInfo(pkgName, PackageManager.GET_META_DATA);
            if ((mPackageInfo.applicationInfo.flags & mPackageInfo.applicationInfo.FLAG_SYSTEM)
                    != 0)
                isSystemApp = true;
            else
                isSystemApp = false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isSystemApp;
    }

    /**
     * createIntent
     *
     * @param context context
     * @param action  action
     * @return intent
     */
    public static Intent createIntent(Context context, String action) throws Exception {

        Intent _intent = new Intent(action);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(_intent, 0);

        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        Intent explicitIntent = new Intent(_intent);
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    /**
     * 获取应用名称
     *
     * @param pkgName
     * @return
     */
    public static String getAppLable(String pkgName) {
        String lable = "";
        try {
            PackageInfo mPackageInfo = getPackageManager().getPackageInfo(pkgName, PackageManager.GET_META_DATA);
            lable = mPackageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        return lable;
    }


}
