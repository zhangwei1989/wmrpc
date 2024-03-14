package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7 22:16
 */
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext context;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = context.getBeansWithAnnotation(WmProvider.class);
        providers.forEach((x, y) -> System.out.println(x));
//        skeleton.putAll(providers);
        providers.values().forEach(x ->
                genInterface(x));
    }

    private void genInterface(Object x) {
        Class<?> itfer = x.getClass().getInterfaces()[0];
        skeleton.put(itfer.getCanonicalName(), x);
    }

    public RpcResponse invoke(RpcRequest request) {
        Object bean = skeleton.get(request.getService());
        RpcResponse response = new RpcResponse();
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            response.setStatus(true);
            response.setData(result);
        } catch (InvocationTargetException e) {
            response.setException(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            response.setException(new RuntimeException(e.getMessage()));
        }

        return response;
    }

    private Method findMethod(Class<?> aClass, String methodName) {
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            if (methodName.contains(method.getName())) {
                return method;
            }
        }

        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
