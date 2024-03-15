package cn.william.wmrpc.core.util;

import java.lang.reflect.Method;
import java.util.Arrays;

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

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                m -> System.out.println(MethodUtils.methodSign(m))
        );
    }
}
