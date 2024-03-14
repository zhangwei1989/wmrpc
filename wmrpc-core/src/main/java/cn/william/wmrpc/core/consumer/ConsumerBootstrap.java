package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
public class ConsumerBootstrap implements ApplicationContextAware {

    private ApplicationContext context;

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
                            proxyService = createProxy(service);
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
    private Object createProxy(Class<?> service) {
        return Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new WmConsumerInvocationHandler(service.getCanonicalName()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
