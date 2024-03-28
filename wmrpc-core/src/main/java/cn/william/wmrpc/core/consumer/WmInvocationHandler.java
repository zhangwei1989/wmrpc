package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.consumer.http.HttpInvoker;
import cn.william.wmrpc.core.consumer.http.OkHttpInvoker;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.util.MethodUtils;
import cn.william.wmrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
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

    List<InstanceMeta> providers;

    HttpInvoker httpInvoker;

    public WmInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
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

        int retries = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retries", "1"));
        while (retries-- > 0) {
            log.info("======> reties: {}", retries);

            try {
                // [
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.prefilter(rpcRequest);
                    if (preResult != null) {
                        log.debug(filter.getClass().getName() + " ======> prefilter: " + preResult);
                        return preResult;
                    }
                }

                //[[
                List<InstanceMeta> instances = context.getRouter().route(providers);
                InstanceMeta instance = context.getLoadBalancer().choose(instances);

                log.info("loadBalancer.choose(urls) ==> {}", instance);
                RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());

                Object result = castResponseToReturnResult(method, rpcResponse);
                // ]
                // 这里拿到的可能不是最终值
                for (Filter filter : this.context.getFilters()) {
                    Object filterResult = filter.postfilter(rpcRequest, rpcResponse, result);

                    if (filterResult != null) {
                        return filterResult;
                    }
                }

                // ]
                // TODO 处理基本类型
                return result;
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return null;
    }

    private static Object castResponseToReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
            if (ex instanceof RpcException exception) {
                throw exception;
            } else {
                throw new RpcException(ex, RpcException.NoSuchMethodEx);
            }
        }
    }

}
