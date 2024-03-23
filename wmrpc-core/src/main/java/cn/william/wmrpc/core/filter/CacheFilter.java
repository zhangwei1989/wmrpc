package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.Filter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/23
 */
@Order(Integer.MAX_VALUE)
public class CacheFilter implements Filter {

    // 替换成 guava cache，加容量和过期时间，过期策略 TODO

    static Map<String, Object> cache = new ConcurrentHashMap();

    @Override
    public Object prefilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
        cache.put(request.toString(), result);
        return result;
    }

}
