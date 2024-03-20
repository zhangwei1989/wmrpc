package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext context;

    @Value("${server.port}")
    private String port;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = context.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
//        skeleton.putAll(providers);
        providers.values().forEach(x ->
                genInterface(x));
    }

    @SneakyThrows
    public void start() {
        RegistryCenter rc = context.getBean(RegistryCenter.class);
        String ip = InetAddress.getLocalHost().getHostAddress();
        skeleton.keySet().stream().forEach(service -> {
            rc.register(service, ip + ":" + port);
        });
    }

    private void genInterface(Object x) {
        Class<?> aClass = x.getClass();
        Arrays.stream(aClass.getInterfaces()).forEach(itfer -> {
            Method[] methods = itfer.getMethods();
            for (Method method : methods) {
                // 如果是 Object 的方法，直接略过
                if (MethodUtils.isLocalMethod(method)) {
                    continue;
                }

                createProvider(itfer.getCanonicalName(), x, method);
            }
        });
    }

    private void createProvider(String serviceName, Object bean, Method method) {
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setBean(bean);
        providerMeta.setMethod(method);

        skeleton.add(serviceName, providerMeta);
    }

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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
