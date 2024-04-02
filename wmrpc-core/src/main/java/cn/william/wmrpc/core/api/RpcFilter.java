package cn.william.wmrpc.core.api;

/**
 * Rpc 过滤器
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public interface RpcFilter {

    Object preFilter(RpcRequest rpcRequest);

    RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse);

    static RpcFilter Default = new RpcFilter() {
        @Override
        public Object preFilter(RpcRequest rpcRequest) {
            return null;
        }

        @Override
        public RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse) {
            return rpcResponse;
        }
    };
}
