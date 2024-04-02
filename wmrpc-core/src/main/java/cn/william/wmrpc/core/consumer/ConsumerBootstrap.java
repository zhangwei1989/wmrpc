package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

    @Autowired
    private RpcContext rpcContext;

    @Autowired
    private RegistryCenter registryCenter;

    @Value("${wmrpc.app}")
    private String app;

    @Value("${wmrpc.namespace}")
    private String namespace;

    @Value("${wmrpc.env}")
    private String env;

    @Value("${wmrpc.version}")
    private String version;

    @Value("${wmrpc.retries}")
    private int retries;

    @Value("${wmrpc.timeout}")
    private int timeout;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {
        String[] names = context.getBeanDefinitionNames();
        // 配置存入 rpcContext 中
        rpcContext.getParameters().put("wmrpc.retires", String.valueOf(retries));
        rpcContext.getParameters().put("wmrpc.timeout", String.valueOf(timeout));

        // RpcFilter 存入 rpcContext 中
        String[] beanNamesForType = context.getBeanNamesForType(RpcFilter.class);
        List<RpcFilter> filters = new ArrayList<>();

        for (String beanName : beanNamesForType) {
            filters.add((RpcFilter) context.getBean(beanName));
        }
        rpcContext.setFilters(filters);

        for (String name : names) {
            Object bean = context.getBean(name);
            Class<?> clazz = bean.getClass();

            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    if (field.isAnnotationPresent(WmConsumer.class)) {
                        Class<?> service = field.getType();
                        String serviceName = service.getCanonicalName();
                        Object proxyService = stub.get(serviceName);

                        if (proxyService == null) {
                            ServiceMeta serviceMeta = ServiceMeta.builder()
                                    .app(app)
                                    .namespace(namespace)
                                    .name(serviceName)
                                    .env(env)
                                    .version(version)
                                    .build();
                            List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
                            log.info("======> consumer fetchAll nodes are: ");
                            providers.stream().forEach(System.out::println);

                            subcribeToRC(serviceMeta, providers);

                            proxyService = createProxy(service, rpcContext, providers);
                        }

                        // 将代理对象设置到打了 @WmConsumer 注解的 field 上
                        field.setAccessible(true);
                        try {
                            field.set(bean, proxyService);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                        stub.put(serviceName, proxyService);
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }
    }

    private void subcribeToRC(ServiceMeta serviceMeta, List<InstanceMeta> providers) {
        registryCenter.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
    }

    // 创建代理对象
    private Object createProxy(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new WmConsumerInvocationHandler(service.getCanonicalName(), rpcContext, providers));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
