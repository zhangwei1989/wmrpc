package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.consumer.http.HttpInvoker;
import cn.william.wmrpc.core.consumer.http.OkHttpInvoker;
import cn.william.wmrpc.core.governance.SlidingTimeWindow;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.util.MethodUtils;
import cn.william.wmrpc.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    final List<InstanceMeta> providers;

    final List<InstanceMeta> isolatedProviders = new ArrayList<>();

    final List<InstanceMeta> halfOpenedProviders = new ArrayList<>();

    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    HttpInvoker httpInvoker;

    ScheduledExecutorService executor;

    public WmInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters().getOrDefault("consumer.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        int halfOpenInitialDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("consumer.halfOpenInitialDelay", "10000"));
        int halfOpenDelay = Integer.parseInt(context.getParameters()
                .getOrDefault("consumer.halfOpenDelay", "60000"));
        this.executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("======>halfOpen isolatedProviders = {}", isolatedProviders);
        halfOpenedProviders.clear();
        halfOpenedProviders.addAll(isolatedProviders);
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
                .getOrDefault("consumer.retries", "1"));
        int faultLimit = Integer.parseInt(context.getParameters().
                getOrDefault("consumer.faultLimit", "5"));
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

                InstanceMeta instance;
                synchronized (halfOpenedProviders) {
                    if (halfOpenedProviders.isEmpty()) {
                        //[[
                        List<InstanceMeta> instances = context.getRouter().route(providers);
                        instance = context.getLoadBalancer().choose(instances);
                        log.info("loadBalancer.choose(urls) ==> {}", instance);
                    } else {
                        instance = halfOpenedProviders.remove(0);
                        log.debug("check alive instance ==> {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse;
                Object result;
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
                    result = castResponseToReturnResult(method, rpcResponse);
                } catch (Exception ex) {
                    // 故障的规则统计和隔离
                    // 每一次异常，记录一次，统计 30s 的异常数
                    String url = instance.toUrl();
                    SlidingTimeWindow window = windows.get(url);
                    if (window == null) {
                        window = new SlidingTimeWindow();
                        windows.put(url, window);
                    }

                    window.record(System.currentTimeMillis());
                    log.debug("instance {} in window with {}", url, window.getSum());

                    // 发生了 10次，就做故障隔离
                    if (window.getSum() >= faultLimit) {
                        isolate(instance);
                    }

                    throw ex;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug("instance {} is recovered, isolatedProviders = {}", instance, isolatedProviders);
                        log.debug("instance {} is recovered, providers = {}", instance, providers);
                    }
                }

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

    private void isolate(InstanceMeta instance) {
        log.debug("======> isolate instance: {}", instance);
        providers.remove(instance);
        log.debug("======> providers = {}", providers);
        isolatedProviders.add(instance);
        log.debug("======> isolatedProviders = {}", isolatedProviders);
    }

    private static Object castResponseToReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            RpcException ex = rpcResponse.getEx();
            if(ex != null) {
                log.error("response error.", ex);
                throw ex;
            }
            return null;
        }
    }

}
