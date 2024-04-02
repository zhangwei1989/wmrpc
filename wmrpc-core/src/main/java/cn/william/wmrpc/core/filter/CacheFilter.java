package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存过滤器
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/4/1
 */
public class CacheFilter implements RpcFilter {

    private final static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        String key = rpcRequest.getService() + rpcRequest.getMethodSign();
        return cache.get(key);
    }

    @Override
    public RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        String key = rpcRequest.getService() + rpcRequest.getMethodSign();
        cache.putIfAbsent(key, rpcResponse);

        return rpcResponse;
    }

}
