package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.util.TypeUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
public class ProviderInvoker {

    private MultiValueMap<String, ProviderMeta> skeleton;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());

            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }

        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if (args == null || args.length == 0) {
            return null;
        }

        Object[] autuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            autuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }

        return autuals;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        return providerMetas.stream().filter(x -> x.getMethodSign().equals(methodSign)).findFirst().orElse(null);
    }
}
