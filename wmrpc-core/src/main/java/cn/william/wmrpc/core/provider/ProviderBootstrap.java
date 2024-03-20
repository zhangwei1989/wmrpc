package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.util.MethodUtils;
import cn.william.wmrpc.core.util.TypeUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    @Autowired
    ApplicationContext context;

    private String instance;

    @Value("${server.port}")
    private String port;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void init() {
        Map<String, Object> providers = context.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
        providers.values().forEach(x ->
                genInterface(x));
    }

    public void start() {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        this.instance = ip + ":" + port;

        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
    }

    private void registerService(String service) {
        RegistryCenter rc = context.getBean(RegistryCenter.class);
        rc.register(service, instance);
    }

    private void unregisterService(String service) {
        RegistryCenter rc = context.getBean(RegistryCenter.class);
        rc.unregister(service, instance);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
