package cn.william.wmrpc.core.utils;

import java.lang.reflect.Method;

/**
 * Method 工具类.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/17
 */
public class MethodUtils {

    public static boolean isLocalMethod(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    public static String getMethodSign(Method method) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(method.getName());
        stringBuilder.append("@");
        stringBuilder.append(method.getParameterCount());

        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {
            stringBuilder.append("_");
            stringBuilder.append(parameterTypes[i].getName());
        }

        return stringBuilder.toString();
    }
}
