package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.loadbalance.LoadBalancer;
import cn.william.wmrpc.core.loadbalance.Router;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    private ApplicationContext context;

    private Environment environment;

    @Autowired
    private RpcContext rpcContext;

    private Router router;

    private LoadBalancer loadBalancer;

    private List<String> providers;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {
        String[] names = context.getBeanDefinitionNames();
        providers = List.of(environment.getProperty("wmrpc.providers").split(","));

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

    // 创建代理对象
    private Object createProxy(Class<?> service, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new WmConsumerInvocationHandler(service.getCanonicalName(), rpcContext, providers));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
