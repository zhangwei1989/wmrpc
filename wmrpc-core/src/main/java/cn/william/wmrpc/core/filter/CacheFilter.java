package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存过滤器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/1
 */
@Slf4j
public class CacheFilter implements RpcFilter {

    private final static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        String key = rpcRequest.getService() + rpcRequest.getMethodSign();
        log.debug("======> return cache rpcResponse: {}", cache.get(key));
        return cache.get(key);
    }

    @Override
    public RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        String key = rpcRequest.getService() + rpcRequest.getMethodSign();
        cache.putIfAbsent(key, rpcResponse);

        return rpcResponse;
    }

}
