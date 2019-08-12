package com.zeasn.remotecontrol.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created: devin.feng
 * 2019-08-07
 * Email: devin.feng@zeasn.com
 * Descripe:
 */
public class ByteToStringUtil {
    /**
     * 把字节数组转化为字符串----"ISO-8859-1"
     *
     * @param data
     * @return
     */

    public static String compress(byte[] data) {
        return compress(byteToString(data));
    }


    public static String byteToString(byte[] data) {
        String dataString = null;
        try {
            //将字节数组转为字符串，编码格式为ISO-8859-1
            dataString = new String(data, "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataString;
    }

    /**
     * 压缩字符串----"ISO-8859-1"
     *
     * @param data
     * @return
     */
    public static String compress(String data) {
        String finalData = null;
        try {
            //打开字节输出流
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            //打开压缩用的输出流,压缩后的结果放在bout中
            GZIPOutputStream gout = new GZIPOutputStream(bout);
            //写入待压缩的字节数组
            gout.write(data.getBytes("ISO-8859-1"));
            //完成压缩写入
            gout.finish();
            //关闭输出流
            gout.close();
            finalData = bout.toString("ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalData;
    }

    /**
     * 2 * 将图片转换为字节数组
     * 3 * @return
     * 4
     */
    public static byte[] loadImage(File file) {
        //用于返回的字节数组
        byte[] data = null;
        //打开文件输入流
        FileInputStream fin = null;
        //打开字节输出流
        ByteArrayOutputStream bout = null;
        try {
            //文件输入流获取对应文件
            fin = new FileInputStream(file);
            //输出流定义缓冲区大小
            bout = new ByteArrayOutputStream((int) file.length());
            //定义字节数组，用于读取文件流
            byte[] buffer = new byte[1024];
            //用于表示读取的位置
            int len = -1;
            //开始读取文件
            while ((len = fin.read(buffer)) != -1) {
                //从buffer的第0位置开始，读取至第len位置，结果写入bout
                bout.write(buffer, 0, len);
            }
            //将输出流转为字节数组
            data = bout.toByteArray();
            //关闭输入输出流
            fin.close();
            bout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 解压字符串
     *
     * @param str
     * @return
     */
    public static String decompressionString(String str) {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        GZIPInputStream gunzip = null;
        try {
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(str
                    .getBytes("ISO-8859-1"));
            gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return out.toString("ISO-8859-1");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                out.close();
                in.close();
                gunzip.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] StringTobyteTest(String data) {
        return StringTobyte(decompressionString(data));
    }

    public static byte[] StringTobyte(String data) {
        byte[] bytes = null;
        try {
            //将字节数组转为字符串，编码格式为ISO-8859-1
            bytes = data.getBytes("ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }


    /**
     * 将bitmap转换成base64字符串
     *
     * @param bitmap
     * @return base64 字符串
     */

    public static String bitmaptoString(Bitmap bitmap, int bitmapQuality) {

        // 将Bitmap转换成字符串

        String string = null;

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, bitmapQuality, bStream);

        byte[] bytes = bStream.toByteArray();

        string = Base64.encodeToString(bytes, Base64.DEFAULT);

        return string;

    }

    /**
     * 将base64转换成bitmap图片
     *
     * @param string base64字符串
     * @return bitmap
     */

    public static Bitmap stringtoBitmap(String string) {

        // 将字符串转换成Bitmap类型

        Bitmap bitmap = null;

        try {

            byte[] bitmapArray;

            bitmapArray = Base64.decode(string, Base64.DEFAULT);

            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return bitmap;

    }

}
