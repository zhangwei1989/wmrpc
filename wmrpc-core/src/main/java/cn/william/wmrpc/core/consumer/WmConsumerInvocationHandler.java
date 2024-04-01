package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
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
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Slf4j
public class WmConsumerInvocationHandler implements InvocationHandler {

//    private static ApplicationContext applicationContext;

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
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::halfOpen, 10, 30, TimeUnit.SECONDS);
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

        int retries = Integer.parseInt(rpcContext.getParameters()
                .getOrDefault("wmrpc.retires", "1"));

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

                RpcResponse rpcResponse;
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
                    if (window.getSum() >= 10) {
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

                if (rpcResponse.isStatus()) {
                    Object data = rpcResponse.getData();
                    return TypeUtils.castMethodResult(method, data);
                } else {
                    Exception ex = rpcResponse.getException();
                    throw ex;
                }
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }

        return null;
    }

}
