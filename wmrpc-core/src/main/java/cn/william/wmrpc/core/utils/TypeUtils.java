package cn.william.wmrpc.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.Nullable;

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

    public static Object castMethodResult(Method method, Object data) {
        if (data instanceof JSONObject) {
            JSON dataJSON = (JSONObject) data;
            return JSON.toJavaObject(dataJSON, method.getReturnType());
        } else if (data instanceof JSONArray array) {
            Object[] originArray = array.toArray();
            Class<?> componentType = method.getReturnType().getComponentType();
            Object result = Array.newInstance(componentType, originArray.length);

            for (int i = 0; i < originArray.length; i++) {
                Array.set(result, i, TypeUtils.cast(originArray[i], componentType));
            }

            return result;
        } else {
            return TypeUtils.cast(data, method.getReturnType());
        }
    }
}
