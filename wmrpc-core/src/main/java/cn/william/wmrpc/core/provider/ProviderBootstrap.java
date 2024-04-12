package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.config.AppConfigProperties;
import cn.william.wmrpc.core.config.ProviderConfigProperties;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ProviderMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import cn.william.wmrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

/**
 * 服务提供者启动类.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    @Autowired
    ApplicationContext applicationContext;

    private InstanceMeta instance;

    private RegistryCenter rc;

    private String port;

    private AppConfigProperties appProperties;

    private ProviderConfigProperties providerProperties;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    public ProviderBootstrap(String port, AppConfigProperties appProperties, ProviderConfigProperties providerProperties) {
        this.port = port;
        this.appProperties = appProperties;
        this.providerProperties = providerProperties;
    }

    @PostConstruct
    public void init() {
        rc = applicationContext.getBean(RegistryCenter.class);
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> log.info(x));
        providers.values().forEach(this::genInterface);
    }

    public void start() {
        String ip;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        rc.start();
        this.instance = InstanceMeta.http(ip, port);
        this.instance.getParameters().putAll(providerProperties.getMetas());
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        log.info("======>unreg all services");
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .name(service)
                .env(appProperties.getEnv())
                .build();
        rc.register(serviceMeta, instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .name(service)
                .env(appProperties.getEnv())
                .build();
        rc.unregister(serviceMeta, instance);
    }

    private void genInterface(Object impl) {
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> {
                    Method[] methods = service.getMethods();
                    for (Method method : methods) {
                        String methodName = method.getName();
                        if (MethodUtils.checkLocalMethod(method)) {   // 过滤掉 Object 的方法
                            continue;
                        }

                        createProvider(service, impl, method);
                    }
                }
        );
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder()
                .serviceImpl(impl)
                .method(method)
                .methodSign(MethodUtils.methodSign(method))
                .build();
        log.info("==========> create a provider {}", providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }

}
