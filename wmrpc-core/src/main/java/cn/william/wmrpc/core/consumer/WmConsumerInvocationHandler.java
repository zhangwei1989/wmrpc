package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Slf4j
public class WmConsumerInvocationHandler implements InvocationHandler {

    private RpcContext rpcContext;

    private final String serviceName;

    private final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json");

    private List<String> providers;

    public WmConsumerInvocationHandler(String serviceName, RpcContext rpcContext, List<String> providers) {
        this.serviceName = serviceName;
        this.rpcContext = rpcContext;
        this.providers = providers;
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

        List<String> ps = rpcContext.getRouter().route(providers);
        String url = (String) rpcContext.getLoadBalancer().choose(ps);

        log.info("==========> loadbalance choose url is {}", url);

        RpcResponse rpcResponse = post(rpcRequest, url);

        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if (data instanceof JSONObject) {
                JSON dataJSON = (JSONObject) data;
                return JSON.toJavaObject(dataJSON, method.getReturnType());
            } else if (data instanceof JSONArray array) {
                Object[] originArray = array.toArray();
                Class<?> componentType = method.getReturnType().getComponentType();
                Object result = Array.newInstance(componentType, originArray.length);

                for (int i = 0; i < originArray.length; i++) {
                    Array.set(result, i, TypeUtils.cast(originArray[i], componentType));
                }

                return result;
            } else {
                return TypeUtils.cast(data, method.getReturnType());
            }
        } else {
            Exception ex = rpcResponse.getException();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    private RpcResponse post(RpcRequest rpcRequest, String url) {
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
