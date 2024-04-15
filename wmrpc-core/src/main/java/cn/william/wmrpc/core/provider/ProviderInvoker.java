package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcException;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.config.ProviderConfigProperty;
import cn.william.wmrpc.core.goverance.SlidingTimeWindow;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.william.wmrpc.core.api.RpcException.TPSLIMIT_EXCEED_ERRCODE;

/**
 * ProviderInvoker
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/22
 */
@Slf4j
public class ProviderInvoker {

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    private MultiValueMap<String, ProviderMeta> skeleton;

    @Autowired
    ProviderConfigProperty providerConfigProperty;

    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    public RpcResponse invoke(RpcRequest request) {
        ProviderMeta providerMeta = findProviderMeta(request);
        // 服务端流控参数
        int tpsLimit = Integer.parseInt(providerConfigProperty.getMetas().getOrDefault("tpsLimit", "10"));
        RpcResponse response = new RpcResponse();
        try {
            Method method = providerMeta.getMethod();

            if (TypeUtils.isLocalMethod(method)) {
                return null;
            }

            // invoker 前执行流控判断逻辑
            windows.putIfAbsent(request.getService(), new SlidingTimeWindow());
            SlidingTimeWindow window = windows.get(request.getService());
            synchronized (window) {
                if (window.calcSum() >= tpsLimit) {
                    log.warn(" ======> the method {} of {} is called more than tpsLimit {}, current called {} times in 30s",
                            request.getMethodSign(), request.getService(), tpsLimit, window.calcSum());
                    throw new RpcException(TPSLIMIT_EXCEED_ERRCODE);
                }

                log.debug(" ======> the method {} of {} is called, current called {} times in 30s",
                        request.getMethodSign(), request.getService(), window.calcSum());
                window.record(System.currentTimeMillis());
            }

            // 处理传参逻辑
            Map<String, String> requestParams = request.getParameters();
            if (!requestParams.isEmpty()) {
                for (String key : requestParams.keySet()) {
                    RpcContext.setContextParams(key, requestParams.get(key));
                }
            }

            Object[] autualArgs = processArgs(method, request.getArgs());
            Object result = method.invoke(providerMeta.getBean(), autualArgs);
            response.setStatus(true);
            response.setData(result);
        } catch (InvocationTargetException e) {
            response.setException(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RpcException(e.getMessage()));
        } finally {
            // 防止内存泄露和上下文污染
            RpcContext.removeContextParams();
        }

        return response;
    }

    private Object[] processArgs(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] actualArgs = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            actualArgs[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }

        return actualArgs;
    }

    private ProviderMeta findProviderMeta(RpcRequest request) {
        List<ProviderMeta> providerMetaList = skeleton.get(request.getService());

        for (ProviderMeta providerMeta : providerMetaList) {
            if (request.getMethodSign().equals(MethodUtils.getMethodSign(providerMeta.getMethod()))) {
                return providerMeta;
            }
        }

        return null;
    }

}
