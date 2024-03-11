package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
public class WmInvocationHandler implements InvocationHandler {

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    public WmInvocationHandler(Class<?> clazz) {
        this.service = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (name.equals("toString") || name.equals("hashCode") || name.equals("equals")) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setArgs(args);

        RpcResponse rpcResponse = post(rpcRequest);

        // TODO 处理基本类型
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data instanceof JSONObject) {
                JSONObject jsonResult = (JSONObject) rpcResponse.getData();
                return jsonResult.toJavaObject(method.getReturnType());
            } else {
                return data;
            }
        } else {
            Exception ex = rpcResponse.getEx();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private RpcResponse post(RpcRequest rpcRequest) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("===> reqJSON: " + reqJson);

        Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("===> respJSON: " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
