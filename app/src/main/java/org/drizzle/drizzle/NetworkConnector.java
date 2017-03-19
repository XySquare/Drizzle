package org.drizzle.drizzle;

import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by ${XYY} on ${2015/11/20}.
 */

public class NetworkConnector {
    private static final String TAG = "NetWorkConnector";

    /**
     * 从指定URL获取原始数据并返回一个字节流数组
     *
     * @param urlSpec 指定URL
     * @return 字节流数组
     * @throws IOException 无法建立链接
     */
    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        //创建一个URL对象
        URL url = new URL(urlSpec);
        //创建一个指向要访问URL的连接对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            //连接到指定的URL地址（GET请求）
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();
            return out.toByteArray();
        } finally {
            //关闭网络连接
            connection.disconnect();
        }
    }

    /**
     * 将getUrlBytes(String)方法返回的结果转换为String
     */
    public static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * 从指定URL获取原始数据并返回一个字节流数组
     *
     * @param urlSpec 指定URL
     * @param content 参数
     * @return 字节流数组
     * @throws IOException 无法建立链接
     */
    public static byte[] postUrlBytes(String urlSpec, String content) throws IOException {
        //创建一个URL对象
        URL url = new URL(urlSpec);
        //创建一个指向要访问URL的连接对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //指定请求方式为POST
        connection.setRequestMethod("POST");
        //使用URL连接输出
        connection.setDoOutput(true);
        try {
            //连接到指定的URL地址（POST请求）
            OutputStream out = connection.getOutputStream();
            //传入参数
            out.write(content.getBytes());
            out.flush();
            out.close();
            //获得响应
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK
                    && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            in.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } finally {
            //关闭网络连接
            connection.disconnect();
        }
    }

    /**
     * 将postUrlBytes(String,String)方法返回的结果转换为String
     */
    public static String postUrlString(String urlSpec, String content) throws IOException {
        return new String(postUrlBytes(urlSpec, content));
    }

    public static byte[] putUrlBytes(String urlSpec, String content) throws IOException {
        //创建一个URL对象
        URL url = new URL(urlSpec);
        //创建一个指向要访问URL的连接对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //指定请求方式为POST
        connection.setRequestMethod("PUT");
        //使用URL连接输出
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", " application/json");
        connection.setRequestProperty("Accept-Charset", "utf-8");
        try {
            //连接到指定的URL地址（PUT请求）
            OutputStream out = connection.getOutputStream();
            //传入参数
            out.write(content.getBytes());
            out.flush();
            out.close();
            //获得响应
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK
                    && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED
                    && connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                throw new IOException(connection.getResponseCode() +" "+ connection.getResponseMessage() + ": with " + urlSpec);
            }
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            in.close();
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } finally {
            //关闭网络连接
            connection.disconnect();
        }
    }

    public static String putUrlString(String urlSpec, String content) throws IOException {
        return new String(putUrlBytes(urlSpec, content));
    }

    public static boolean deleteUrl(String urlSpec) throws IOException {
        boolean respond  = true;
        //创建一个URL对象
        URL url = new URL(urlSpec);
        //创建一个指向要访问URL的连接对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //指定请求方式为POST
        connection.setRequestMethod("DELETE");
        try {
            //连接到指定的URL地址（DELETE请求）
            connection.connect();
            //获得响应
            if (connection.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
                respond = false;
                Log.e(TAG,connection.getResponseCode()+" "+connection.getResponseMessage() + ": with " + urlSpec);
            }
        } finally {
            //关闭网络连接
            connection.disconnect();
        }
        return respond;
    }

    /*public static int deleteUrlString(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("DELETE");
        connection.connect();

        return connection.getResponseCode();
    }*/
}
