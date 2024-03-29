package cn.william.wmrpc.core.client;

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
 * @Create : 2024/3/22
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .build();

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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
