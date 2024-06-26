package cn.william.wmrpc.core.client;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.TimeUnit;

/**
 * OkHttpInvoker 类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/22
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private final OkHttpClient client;

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient().newBuilder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .build();
    }

    private final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

    @Override
    public RpcResponse post(RpcRequest rpcRequest, String url) {
        // JSON 序列化
        String requestJSON = JSON.toJSONString(rpcRequest);
        log.info("=============> requestJSON: {}", requestJSON);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestJSON, MEDIA_TYPE_JSON))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseJSON = response.body().string();
            log.info("=============> responseJSON: {}", responseJSON);
            return JSON.parseObject(responseJSON, RpcResponse.class);
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
