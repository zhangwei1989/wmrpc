package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcFilter;
import cn.william.wmrpc.core.config.AppConfigProperty;
import cn.william.wmrpc.core.config.ConsumerConfigProperty;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费者启动类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/7 22:16
 */
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware {

    private AppConfigProperty appConfigProperty;

    private ConsumerConfigProperty consumerConfigProperty;

    private ApplicationContext context;

    private Map<String, Object> stub = new HashMap<>();

    public ConsumerBootstrap(AppConfigProperty appConfigProperty, ConsumerConfigProperty consumerConfigProperty) {
        this.appConfigProperty = appConfigProperty;
        this.consumerConfigProperty = consumerConfigProperty;
    }

    public void start() {
        String[] names = context.getBeanDefinitionNames();
        RpcContext rpcContext = context.getBean(RpcContext.class);
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);

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
                                    .app(appConfigProperty.getId())
                                    .namespace(appConfigProperty.getNamespace())
                                    .name(serviceName)
                                    .env(appConfigProperty.getEnv())
                                    .version(appConfigProperty.getVersion())
                                    .build();
                            List<InstanceMeta> providers = registryCenter.fetchAll(serviceMeta);
                            log.info("======> consumer fetchAll nodes are: ");
                            providers.stream().forEach(System.out::println);

                            subcribeToRC(registryCenter, serviceMeta, providers);

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

    private void subcribeToRC(RegistryCenter registryCenter, ServiceMeta serviceMeta, List<InstanceMeta> providers) {
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
