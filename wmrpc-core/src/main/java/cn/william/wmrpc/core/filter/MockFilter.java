package cn.william.wmrpc.core.filter;

import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/2
 */
public class MockFilter implements RpcFilter {

    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        // 获取需要 mock 的类型
        Class clazz = Class.forName(rpcRequest.getService());
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (rpcRequest.getMethodSign().equals(MethodUtils.getMethodSign(method))) {
                Class<?> returnType = method.getReturnType();
                Object data = MockUtils.mock(returnType);
                RpcResponse response = new RpcResponse();
                response.setStatus(true);
                response.setData(data);

                return response;
            }
        }

        return null;
    }

    @Override
    public RpcResponse postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse) {
        return rpcResponse;
    }

}
