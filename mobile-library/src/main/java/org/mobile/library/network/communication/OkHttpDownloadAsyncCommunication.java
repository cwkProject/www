package org.mobile.library.network.communication;
/**
 * Created by 超悟空 on 2015/11/30.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.mobile.library.global.GlobalApplication;
import org.mobile.library.network.util.AsyncCommunication;
import org.mobile.library.network.util.NetworkCallback;
import org.mobile.library.network.util.NetworkProgressListener;
import org.mobile.library.network.util.NetworkRefreshProgressHandler;
import org.mobile.library.network.util.NetworkTimeoutHandler;
import org.mobile.library.network.util.ProgressResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于OkHttp实现的异步文件下载请求通讯组件类，
 * 默认文件下载类，不可扩展，
 * 使用get请求访问下载地址
 *
 * @author 超悟空
 * @version 1.0 2015/11/30
 * @since 1.0
 */
public class OkHttpDownloadAsyncCommunication implements AsyncCommunication<Map<String, String>,
        InputStream>, NetworkRefreshProgressHandler, NetworkTimeoutHandler {

    /**
     * 日志标签前缀
     */
    private static final String LOG_TAG = "OkHttpDownloadAsyncCommunication.";

    /**
     * 当前网络请求标签
     */
    private String tag = UUID.randomUUID().toString();

    /**
     * 请求地址的完整路径
     */
    protected String url = null;

    /**
     * 请求数据编码，默认使用UTF-8
     */
    protected String encoded = "UTF-8";

    /**
     * 请求超时时间
     */
    protected int timeout = -1;

    /**
     * 下载进度监听器
     */
    private NetworkProgressListener progressListener = null;

    /**
     * 读取超时时间
     */
    protected int readTimeout = 0;

    /**
     * 设置读取超时时间
     *
     * @param readTimeout 超时时间，单位毫秒
     */
    @Override
    public void setReadTimeout(int readTimeout) {
        Log.i(LOG_TAG + "setReadTimeout", "readTimeout is " + readTimeout);
        this.readTimeout = readTimeout;
    }

    @Override
    public void setNetworkProgressListener(NetworkProgressListener networkProgressListener) {
        this.progressListener = networkProgressListener;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间，单位毫秒
     */
    @Override
    public void setTimeout(int timeout) {
        Log.i(LOG_TAG + "setTimeout", "timeout is " + timeout);
        this.timeout = timeout;
    }

    /**
     * 设置编码格式
     *
     * @param encoded 编码字符串，默认为UTF-8
     */
    public void setEncoded(String encoded) {
        Log.i(LOG_TAG + "setEncoded", "encoded is " + encoded);
        this.encoded = encoded;
    }

    /**
     * 设置请求地址
     *
     * @param url 完整地址
     */
    @Override
    public void setTaskName(String url) {
        this.url = url;
        Log.i(LOG_TAG + "setTaskName", "url is " + this.url);
    }

    @Override
    public void Request(Map<String, String> sendData, final NetworkCallback<InputStream> callback) {
        Log.i(LOG_TAG + "Request", "Request start");
        Log.i(LOG_TAG + "Request", "url is " + url);

        if (url == null || !url.trim().toLowerCase().startsWith("http://")) {
            // 地址不合法
            Log.d(LOG_TAG + "Request", "url is error");

            if (callback != null) {
                callback.onFinish(false, null);
            }

            return;
        }

        // 拼接参数
        String params = onBuildParameter(sendData);


        // 最终请求地址
        String finalUrl = params.length() == 0 ? url : url + "?" + params;
        Log.i(LOG_TAG + "Request", "final url is " + finalUrl);

        // 得到okHttpClient对象
        OkHttpClient okHttpClient = GlobalApplication.getOkHttpClient();

        // 创建请求
        Request request = new Request.Builder().tag(tag).url(finalUrl).build();

        okHttpClient = okHttpClient.clone();
        // 设置读取超时时间
        if (timeout > -1) {
            okHttpClient.setReadTimeout(readTimeout, TimeUnit.MILLISECONDS);
        }

        if (progressListener != null) {
            // 增加拦截器监听下载进度
            okHttpClient.networkInterceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder().body(new ProgressResponseBody
                            (originalResponse.body(), progressListener)).build();
                }
            });
        }

        // 判断是否需要设置超时
        if (timeout > -1) {
            okHttpClient.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
        }

        // 发送请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(LOG_TAG + "Request", "onFailure IOException type is " + e.toString());
                Log.e(LOG_TAG + "Request", "onFailure IOException message is " + e.getMessage());

                if (callback != null) {
                    callback.onFinish(false, null);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(LOG_TAG + "Request", "onResponse response code is " + response.code());
                Log.i(LOG_TAG + "Request", "onResponse response message is " + response.message());
                if (callback != null) {

                    if (response.isSuccessful()) {
                        Log.i(LOG_TAG + "Request", "request is success");

                        callback.onFinish(true, response.body().byteStream());

                        // 关闭流，回调方法中需要同步处理流
                        response.body().close();
                    } else {
                        Log.i(LOG_TAG + "Request", "request is failed");
                        callback.onFinish(false, null);
                    }
                }
            }
        });

    }

    /**
     * 拼接参数字符串
     *
     * @param sendData 请求参数对
     *
     * @return 拼接完成的字符串
     */
    @NonNull
    private String onBuildParameter(Map<String, String> sendData) {
        // 请求参数装配器参数
        StringBuilder params = new StringBuilder();

        try {

            // 遍历sendData集合并加入请求参数对象
            if (sendData != null && !sendData.isEmpty()) {
                Log.i(LOG_TAG + "onBuildParameter", "sendData count is " + sendData.size());

                // 遍历并追加参数
                for (Map.Entry<String, String> dataEntry : sendData.entrySet()) {
                    params.append(dataEntry.getKey());
                    params.append('=');
                    if (dataEntry.getValue() != null && dataEntry.getValue().length() > 0) {
                        params.append(URLEncoder.encode(dataEntry.getValue(), encoded));
                    }
                    params.append('&');
                }
                // 移除末尾的'&'
                params.deleteCharAt(params.length() - 1);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG + "onBuildParameter", "response error IOException class is " + e
                    .toString());
            Log.e(LOG_TAG + "onBuildParameter", "response error IOException message is " + e
                    .getMessage());
        }
        return params.toString();
    }

    @Override
    public void cancel() {
        GlobalApplication.getOkHttpClient().cancel(tag);
    }
}