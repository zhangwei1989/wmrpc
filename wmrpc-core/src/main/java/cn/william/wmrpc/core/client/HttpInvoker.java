package cn.william.wmrpc.core.client;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/22
 */
public interface HttpInvoker {

    public RpcResponse post(RpcRequest rpcRequest, String url);
}
