package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.LoadBalancer;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.api.RpcContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private Router router;

    private LoadBalancer loadBalancer;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {
        String[] names = context.getBeanDefinitionNames();

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
                            List<String> nodes = registryCenter.fetchAll(serviceName);
                            log.info("======> consumer fetchAll nodes are: ");
                            nodes.stream().forEach(System.out::println);
                            List<String> providers = mapUrls(nodes);

                            subcribeToRC(serviceName, providers);

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

    private void subcribeToRC(String serviceName, List<String> providers) {
        registryCenter.subscribe(serviceName, event -> {
            providers.clear();
            providers.addAll(mapUrls(event.getData()));
        });
    }

    private List<String> mapUrls(List<String> nodes) {
        return nodes.stream().map(x -> "http://" + x).collect(Collectors.toList());
    }

    // 创建代理对象
    private Object createProxy(Class<?> service, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new WmConsumerInvocationHandler(service.getCanonicalName(), rpcContext, providers));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}
