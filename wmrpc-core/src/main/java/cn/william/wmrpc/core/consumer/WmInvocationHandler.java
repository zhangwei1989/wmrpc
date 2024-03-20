package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.consumer.http.HttpInvoker;
import cn.william.wmrpc.core.consumer.http.OkHttpInvoker;
import cn.william.wmrpc.core.util.MethodUtils;
import cn.william.wmrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 消费端动态代理类
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
@Slf4j
public class WmInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext context;

    List<String> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public WmInvocationHandler(Class<?> clazz, RpcContext context, List<String> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (name.equals("toString") || name.equals("h ashCode") || name.equals("equals")) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        List<String> urls = context.getRouter().route(providers);
        String url = (String) context.getLoadBalancer().choose(urls);

        log.info("loadBalancer.choose(urls) ==> {}", url);
        RpcResponse rpcResponse = httpInvoker.post(rpcRequest, url);

        // TODO 处理基本类型
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data, rpcResponse);
        } else {
            Exception ex = rpcResponse.getEx();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
