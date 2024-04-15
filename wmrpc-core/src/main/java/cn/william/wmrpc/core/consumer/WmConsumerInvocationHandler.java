package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.client.OkHttpInvoker;
import cn.william.wmrpc.core.goverance.SlidingTimeWindow;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 消费者远程调用代理增强类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/13
 */
@Slf4j
public class WmConsumerInvocationHandler implements InvocationHandler {

    private RpcContext rpcContext;

    private final String serviceName;

    private List<InstanceMeta> providers;

    private Set<InstanceMeta> isolatedProviders = new HashSet<>();

    private List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    private Map<String, SlidingTimeWindow> windows = new HashMap<>();

    private OkHttpInvoker okHttpInvoker;

    private ScheduledExecutorService executor;

    public WmConsumerInvocationHandler(String serviceName, RpcContext rpcContext, List<InstanceMeta> providers) {
        /*if (WmConsumerInvocationHandler.applicationContext == null) {
            WmConsumerInvocationHandler.applicationContext = new AnnotationConfigApplicationContext();
        }*/
        this.serviceName = serviceName;
        this.rpcContext = rpcContext;
        this.providers = providers;
        okHttpInvoker = new OkHttpInvoker(
                Integer.parseInt(rpcContext.getParameters().getOrDefault("wmrpc.timeout", "1000")));
        int halfOpenInitialDelay = Integer.parseInt(
                rpcContext.getParameters().getOrDefault("wmrpc.halfOpenInitialDelay", "1000"));
        int halfOpenDelay = Integer.parseInt(
                rpcContext.getParameters().getOrDefault("wmrpc.halfOpenDelay", "30"));
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::halfOpen, halfOpenInitialDelay, halfOpenDelay, TimeUnit.SECONDS);
    }

    private void halfOpen() {
        log.debug("halfOpen current isolatedProviders: {}", isolatedProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolatedProviders);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        okHttpInvoker = WmConsumerInvocationHandler.applicationContext.getBean(OkHttpInvoker.class);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(this.serviceName);
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setArgs(args);

        RpcResponse rpcResponse;
        // 执行前置过滤器逻辑
        for (RpcFilter filter : rpcContext.getFilters()) {
            Object preResult = filter.preFilter(rpcRequest);
            if (preResult != null) {
                rpcResponse = (RpcResponse) preResult;

                log.debug("======> return filter rpcResponse: {}", rpcResponse);
                return castRpcResponseToResult(method, rpcResponse);
            }
        }

        int retries = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("wmrpc.retires", "1"));
        int faultLimit = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("wmrpc.faultLimit", "10"));

        while (retries-- > 0) {
            try {
                log.info("======> retries: {}", retries);

                InstanceMeta instance;
                if (halfOpenProviders.isEmpty()) {
                    List<InstanceMeta> instanceMetas = rpcContext.getRouter().route(providers);
                    instance = rpcContext.getLoadBalancer().choose(instanceMetas);
                } else {
                    // 选取第一个
                    instance = halfOpenProviders.remove(0);
                }

                log.info("==========> loadbalance choose url is {}", instance.http());

                try {
                    rpcResponse = okHttpInvoker.post(rpcRequest, instance.http());
                } catch (Exception ex) {
                    // 请求失败，就加入滑动窗口
                    String path = instance.toPath();
                    SlidingTimeWindow window = windows.putIfAbsent(path, new SlidingTimeWindow());
                    if (window == null) {
                        window = windows.get(path);
                    }
                    window.record(System.currentTimeMillis());
                    log.info("======> instance {} added to window, current sum is {}", path, window.getSum());

                    // 满足设置的最高次数，则进行故障隔离设置
                    if (window.getSum() >= faultLimit) {
                        isolatedProviders.add(instance);
                        providers.remove(instance);
                        log.debug("======> complete fault tolenance, current isolatedProviders: {}", isolatedProviders);
                        log.debug("======> complete fault tolenance, current providers: {}", providers);
                    }

                    throw ex;
                }

                // 如果请求成功，就把 instance 加回 providers
                if (!providers.contains(instance)) {
                    log.info("======> instance is recovered, instance: {}", instance);
                    isolatedProviders.remove(instance);
                    providers.add(instance);
                }

                for (RpcFilter filter : rpcContext.getFilters()) {
                    rpcResponse = filter.postFilter(rpcRequest, rpcResponse);
                }

                return castRpcResponseToResult(method, rpcResponse);
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return null;
    }

    private static Object castRpcResponseToResult(Method method, RpcResponse rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            RpcException ex = rpcResponse.getException();
            throw ex;
        }
    }

}
