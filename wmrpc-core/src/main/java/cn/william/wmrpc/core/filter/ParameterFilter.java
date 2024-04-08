package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.Filter;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 上下文参数过滤器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/8
 */
public class ParameterFilter implements Filter {

    @Override
    public Object prefilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if(!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.ContextParameters.get().clear();
        return result;
    }

}
