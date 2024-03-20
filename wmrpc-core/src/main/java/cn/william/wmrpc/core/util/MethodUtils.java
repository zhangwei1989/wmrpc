package cn.william.wmrpc.core.util;

import cn.william.wmrpc.core.annotation.WmConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/15
 */
public class MethodUtils {

    public static boolean checkLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);

    }
    public static String methodSign(Method method) {
        String name = method.getName();
        StringBuilder sb = new StringBuilder(method.getName());
        sb.append("@").append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> sb.append("_").append(c.getCanonicalName())
        );
        return sb.toString();
    }

    public static String methodSign(Method method, Class clazz) {
        return null;
    }

    public static List<Field> findAnnotatedField(Class<?> aClass, Type annotation) {
        List<Field> result = new ArrayList<>();

        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(WmConsumer.class)) {
                    result.add(f);
                }
            }

            aClass = aClass.getSuperclass();
        }

        return result;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(MethodUtils.methodSign(m))
        );
    }
}
