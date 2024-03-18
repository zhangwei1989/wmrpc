package cn.william.wmrpc.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/17
 */
public class TypeUtils {

    public static Object cast(Object origin, Class<?> targetType) {
        if (Objects.isNull(origin)) {
            return null;
        }

        if (origin.getClass().isAssignableFrom(targetType)) {
            return origin;
        }

        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(targetType);
        }

        if (targetType.isArray()) {
            List originArray = (List) origin;
            Class<?> componentType = targetType.getComponentType();
            Object result = Array.newInstance(componentType, originArray.size());

            for (int i = 0; i < originArray.size(); i++) {
                Array.set(result, i, TypeUtils.cast(originArray.get(i), componentType));
            }

            return result;
        }

        if (Integer.class.equals(targetType) || Integer.TYPE.equals(targetType)) {
            return Integer.valueOf(origin.toString());
        } else if (Short.class.equals(targetType) || Short.TYPE.equals(targetType)) {
            return Short.valueOf(origin.toString());
        } else if (Double.class.equals(targetType) || Double.TYPE.equals(targetType)) {
            return Double.valueOf(origin.toString());
        } else if (Long.class.equals(targetType) || Long.TYPE.equals(targetType)) {
            return Long.valueOf(origin.toString());
        } else if (Float.class.equals(targetType) || Float.TYPE.equals(targetType)) {
            return Float.valueOf(origin.toString());
        } else if (Byte.class.equals(targetType) || Byte.TYPE.equals(targetType)) {
            return Byte.valueOf(origin.toString());
        } else if (Character.class.equals(targetType) || Character.TYPE.equals(targetType)) {
            return origin.toString().toCharArray()[0];
        }

        return null;
    }

    public static boolean isLocalMethod(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }
}
