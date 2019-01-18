package com.zeasn.remotecontrol.utils;

import android.app.Service;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;


import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MethodUtils {

    /**
     * 根据签名判断是否能安装插件
     */
    public static boolean isPluginAvailable(Context context) {
        String key = "";
        PackageManager pm = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = pm.getPackageInfo("com.android.defcontainer", 64);
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            MLog.e("isPluginAvailable+NameNotFoundException");
        }
        String packageName = info.packageName;
        /************** 得到应用签名 **************/
        key = (MD5Encryption.getMD5(info.signatures[0].toByteArray()));

        // 按包名 取签名
        Log.i("sig", "pkg>>>  " + packageName + "  ,key>>>  " + key);
        return key.equals("8DDB342F2DA5408402D7568AF21E29F9");
    }

    /**
     * 根据签名判断是否为原生签名
     */
    public static boolean isPrimarySigAvailable(Context context) {
        String sysKey = "";
        String myKey = "";
        PackageManager pm = context.getPackageManager();
        PackageInfo sysInfo = null;
        PackageInfo myInfo = null;
        try {
            MLog.e("isPrimarySigAvailable-----::" + Const.PKG_NAME);
            sysInfo = pm.getPackageInfo("com.android.defcontainer", 64);
            myInfo = pm.getPackageInfo(Const.PKG_NAME, 64);

        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            MLog.e("isPrimarySigAvailable+NameNotFoundException");
        }
//        String packageName = sysInfo.packageName;
        /************** 得到应用签名 **************/
        sysKey = (MD5Encryption.getMD5(sysInfo.signatures[0].toByteArray()));
        myKey = (MD5Encryption.getMD5(myInfo.signatures[0].toByteArray()));

        return sysKey.equals(myKey);
    }

    private static String getLocalIpAddress() {
        String ipaddress = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && !inetAddress.isLinkLocalAddress()
                            && InetAddressUtils.isIPv4Address(inetAddress
                            .getHostAddress())) {
                        ipaddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return ipaddress;
    }

    public static String getIp(Context ctx) {
        WifiManager wifi = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return isConnectWifi(ctx) ? intToIp(info.getIpAddress())
                : getLocalIpAddress();
    }

    public static String getQRMessage(Context ctx) {
        Build bd = new Build();
        String model = bd.MODEL;

        AudioManager audio = (AudioManager) ctx
                .getSystemService(Service.AUDIO_SERVICE);
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        int now = audio.getStreamVolume(AudioManager.STREAM_MUSIC); //
        // 获取系统当前音量

        int encodeIp = encodeIp(getIp(ctx));

//		StringBuilder msg = new StringBuilder("http://file.51vapp.com/download/mtscreen/RemoteControlPhoneClient_1.06.apk?");
//		StringBuilder msg = new StringBuilder("http://10.8.10.216:8080/mtsc/api/m/scan?");// 内网地址
        StringBuilder msg = new StringBuilder("http://124.207.138.156:8080/mtsc/api/m/scan?");//外网地址
//		msg.append("n=");
//		msg.append(model); // 设备名
//		msg.append("&i=");
//		msg.append(Integer.toString(encodeIp, 36)); // ip地址
//		msg.append("&v=");
//		msg.append(Integer.toString(max, 36)); // 设备的最大音量
//		msg.append("&cv=");
//		msg.append(Integer.toString(now, 36)); // 设备的当前音量

        //ip 明文显示
//        msg.append("n=");
//        msg.append(model); // 设备名
//        msg.append("&i=");
//        msg.append(getIp(ctx)); // ip地址
        msg.append("&v=");
        msg.append(max); // 设备的最大音量
        msg.append("&cv=");
        msg.append(now); // 设备的当前音量

        return msg.toString();
    }

    /**
     * 将String 型ip转换成int型
     *
     * @param ip
     * @return
     */
    private static int encodeIp(String ip) {
        int i = 0;
        int j;
        try {
            int k = ip.indexOf('.');
            int l = ip.indexOf('.', k + 1);
            int i1 = ip.indexOf('.', l + 1);
            i = 0x0 | 0xFF & Integer.parseInt(ip.substring(0, k));
            i |= (0xFF & Integer.parseInt(ip.substring(k + 1, l))) << 8;
            i |= (0xFF & Integer.parseInt(ip.substring(l + 1, i1))) << 16;
            int i2 = Integer.parseInt(ip.substring(i1 + 1));
            j = i | (i2 & 0xFF) << 24;
            return j;
        } catch (Exception localException) {
            j = i;
            localException.printStackTrace();
            return 0;
        }
    }

    public static String getWifiSSID(Context ctx) {
        WifiManager wifi = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getSSID();
    }

    public static boolean isConnectWifi(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return wifiNetworkInfo.isConnected()
                && ConnectivityManager.TYPE_WIFI == info.getType();

    }

    public static String getLocalMacAddressFromWifiInfo(Context context)

    {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String macAdress = info.getMacAddress(); // 获取mac地址
        return macAdress;
    }

    private static String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

}
