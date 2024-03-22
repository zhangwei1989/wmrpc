package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.utils.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
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

    ApplicationContext context;

    private RegistryCenter rc;

    @Value("${server.port}")
    private String port;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void buildProviders() {
        rc = context.getBean(RegistryCenter.class);
        Map<String, Object> providers = context.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
//        skeleton.putAll(providers);
        providers.values().forEach(x ->
                genInterface(x));
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        rc.start();
        skeleton.keySet().stream().forEach(service -> {
            rc.register(service, ip + ":" + port);
        });
    }

    @SneakyThrows
    @PreDestroy
    public void stop() {
        RegistryCenter rc = context.getBean(RegistryCenter.class);
        String ip = InetAddress.getLocalHost().getHostAddress();
        skeleton.keySet().stream().forEach(service -> {
            rc.unregister(service, ip + ":" + port);
        });
        rc.stop();
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
