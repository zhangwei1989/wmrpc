package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.utils.MethodUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Slf4j
public class WmConsumerInvocationHandler implements InvocationHandler {

    private final String serviceName;

    private final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

    public WmConsumerInvocationHandler(String serviceName) {
        this.serviceName = serviceName;
    }

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .build();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(this.serviceName);
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setArgs(args);

        RpcResponse rpcResponse = post(rpcRequest);

        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data instanceof JSONObject) {
                JSON dataJSON = (JSONObject) data;
                return JSON.toJavaObject(dataJSON, method.getReturnType());
            } else {
                return data;
            }
        } else {
            Exception ex = rpcResponse.getException();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private RpcResponse post(RpcRequest rpcRequest) {
        // JSON 序列化
        String requestJSON = JSON.toJSONString(rpcRequest);
        log.info("=============> requestJSON: {}", requestJSON);

        Request request = new Request.Builder()
                .url("http://localhost:8080")
                .post(RequestBody.create(requestJSON, MEDIA_TYPE_JSON))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseJSON = response.body().string();
            log.info("=============> responseJSON: {}", responseJSON);
            return JSON.parseObject(responseJSON, RpcResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
