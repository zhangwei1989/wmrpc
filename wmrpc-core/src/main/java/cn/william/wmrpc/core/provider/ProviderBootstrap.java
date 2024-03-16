package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.util.MethodUtils;
import cn.william.wmrpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    @Autowired
    ApplicationContext context;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void start() {
        Map<String, Object> providers = context.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
//        skeleton.putAll(providers);
        providers.values().forEach(x ->
                genInterface(x));
    }

    private void genInterface(Object x) {
        Arrays.stream(x.getClass().getInterfaces()).forEach(
                itfer -> {
                    Method[] methods = itfer.getMethods();
                    for (Method method : methods) {
                        String methodName = method.getName();
                        if (MethodUtils.checkLocalMethod(method)) {   // 过滤掉 Object 的方法
                            continue;
                        }

                        createProvider(itfer, x, method);
                    }
                }
        );
    }

    private void createProvider(Class<?> itfer, Object x, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setMethodSign(MethodUtils.methodSign(method));
        providerMeta.setServiceImpl(x);
        log.info("==========> create a provider {}", providerMeta);
        skeleton.add(itfer.getCanonicalName(), providerMeta);
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
