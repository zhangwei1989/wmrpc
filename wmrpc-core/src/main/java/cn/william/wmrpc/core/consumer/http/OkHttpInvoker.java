package cn.william.wmrpc.core.consumer.http;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    OkHttpClient client;

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    public OkHttpInvoker(int timeout) {
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    @Override
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.info("===> reqJSON: " + reqJson);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.info("===> respJSON: " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String post(String requestBody, String url) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("OkHttpInvoker post, url, requestBody, respJson ======> {}, {}, {}", url, requestBody, respJson);
            return respJson;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug("OkHttpInvoker get, url, respJson ======> {}, {}", url, respJson);
            return respJson;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
