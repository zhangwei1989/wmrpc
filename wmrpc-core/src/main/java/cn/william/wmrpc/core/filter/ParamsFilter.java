package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import lombok.Data;

/**
 * 消费者端参数拦截器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/15
 */
@Data
public class ParamsFilter implements RpcFilter {

    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        rpcRequest.getParameters().putAll(RpcContext.getContextParams());
        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        RpcContext.removeContextParams();
        return rpcResponse;
    }

}
