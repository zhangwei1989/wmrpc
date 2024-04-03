package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
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
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/7 22:16
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext context;

    private RegistryCenter rc;

    @Value("${server.port}")
    private Integer port;

    @Value("${wmrpc.app}")
    private String app;

    @Value("${wmrpc.namespace}")
    private String namespace;

    @Value("${wmrpc.env}")
    private String env;

    @Value("${wmrpc.version}")
    private String version;

    private InstanceMeta instanceMeta;

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
        instanceMeta = InstanceMeta.builder().scheme("http").host(ip).port(port).build();
        rc.start();
        skeleton.keySet().stream().forEach(service -> {
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .app(app)
                    .namespace(namespace)
                    .name(service)
                    .env(env)
                    .version(version)
                    .build();
            rc.register(serviceMeta, instanceMeta);
        });
    }

    @SneakyThrows
    @PreDestroy
    public void stop() {
        RegistryCenter rc = context.getBean(RegistryCenter.class);
        skeleton.keySet().stream().forEach(service -> {
            ServiceMeta serviceMeta = ServiceMeta.builder()
                    .app(app)
                    .namespace(namespace)
                    .name(service)
                    .env(env)
                    .version(version)
                    .build();
            rc.unregister(serviceMeta, instanceMeta);
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
