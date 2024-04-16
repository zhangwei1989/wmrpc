package cn.william.wmrpc.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 类型转换工具类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/17
 */
public class TypeUtils {

    public static Object cast(Object origin, Class<?> targetType) {
        if (Objects.isNull(origin)) {
            return null;
        }

        if (targetType.isAssignableFrom(origin.getClass())) {
            return origin;
        }

        if (origin instanceof Map map) {
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
        } else if (Boolean.class.equals(targetType) || Boolean.TYPE.equals(targetType)) {
            return Boolean.valueOf(origin.toString());
        }

        return null;
    }

    public static Object castWithGenericType(Object origin, Class<?> targetType, Type genericType) {
        if (origin instanceof List || targetType.isArray()) {
            List originArray = (List) origin;
            if (targetType.isArray()) {
                Class<?> componentType = targetType.getComponentType();
                Object result = Array.newInstance(componentType, originArray.size());

                for (int i = 0; i < originArray.size(); i++) {
                    Array.set(result, i, TypeUtils.cast(originArray.get(i), componentType));
                }

                return result;
            } else if (List.class.isAssignableFrom(targetType)) {
                List<Object> result = new ArrayList<>(originArray.size());
                if (genericType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    for (Object o : originArray) {
                        result.add(cast(o, (Class<?>) actualType));
                    }
                } else {
                    result.addAll(originArray);
                }

                return result;
            }
        } else if (Map.class.isAssignableFrom(origin.getClass()) && Map.class.isAssignableFrom(targetType)) {
            // 目标类型是 Map，而不是 POJO，需要处理泛型的逻辑
            Map<Object, Object> resultMap = new HashMap<>();
            if (genericType instanceof ParameterizedType parameterizedType) {
                Type keyType = parameterizedType.getActualTypeArguments()[0];
                Type valueType = parameterizedType.getActualTypeArguments()[1];
                Map<Object, Object> originMap = (Map) origin;
                originMap.forEach((k, v) -> {
                    Object autualKey = cast(k, (Class<?>) keyType);
                    Object autualValue = cast(v, (Class<?>) valueType);
                    resultMap.put(autualKey, autualValue);
                });

                return resultMap;
            }
        }

        return cast(origin, targetType);
    }

    public static boolean isLocalMethod(Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    public static Object castMethodResult(Method method, Object data) {
        Class<?> returnType = method.getReturnType();
        Type genericType = method.getGenericReturnType();
        return TypeUtils.castWithGenericType(data, returnType, genericType);
    }
}
