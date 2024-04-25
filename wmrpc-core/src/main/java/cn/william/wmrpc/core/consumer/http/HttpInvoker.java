package cn.william.wmrpc.core.consumer.http;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker DEFAULT = new OkHttpInvoker(3000);

    String post(String requestBody, String url);

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

    public String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug("httpGet, url ======> {}", url);
        String respJson = DEFAULT.get(url);
        log.debug("httpGet, url, respJson ======> {}, {}", url, respJson);
        return JSON.parseObject(respJson, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug("httpGet, url ======> {}", url);
        String respJson = DEFAULT.get(url);
        log.debug("httpGet, url, respJson ======> {}, {}", url, respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    static <T> T httpPost(String requestBody, String url, Class<T> clazz) {
        log.debug("httpPost, url, requestBody ======> {}, {}", url, requestBody);
        String respJson = DEFAULT.post(requestBody, url);
        log.debug("httpPost, url, requestBody, respJson ======> {}, {}, {}", url, requestBody, respJson);
        return JSON.parseObject(respJson, clazz);
    }

}
