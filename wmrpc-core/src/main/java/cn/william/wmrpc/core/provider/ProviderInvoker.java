package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/22
 */
public class ProviderInvoker {

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    private MultiValueMap<String, ProviderMeta> skeleton;

    public RpcResponse invoke(RpcRequest request) {
        ProviderMeta providerMeta = findProviderMeta(request);
        RpcResponse response = new RpcResponse();
        try {
            Method method = providerMeta.getMethod();

            if (TypeUtils.isLocalMethod(method)) {
                return null;
            }

            Object[] autualArgs = processArgs(method, request.getArgs());

            Object result = method.invoke(providerMeta.getBean(), autualArgs);
            response.setStatus(true);
            response.setData(result);
        } catch (InvocationTargetException e) {
            response.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RuntimeException(e.getMessage()));
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
