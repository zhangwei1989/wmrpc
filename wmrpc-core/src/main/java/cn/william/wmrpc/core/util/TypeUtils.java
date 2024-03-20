package cn.william.wmrpc.core.util;

import cn.william.wmrpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/16
 */
public class TypeUtils {

    public static Object cast(Object origin, Class<?> type) {
        if (origin == null) {
            return null;
        }

        Class<?> aclass = origin.getClass();

        if (type.isAssignableFrom(aclass)) {
            return origin;
        }

        if (type.isArray()) {
            if (origin instanceof List list) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();
            Object resultArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                Array.set(resultArray, i, Array.get(origin, i));
            }

            return resultArray;
        }

        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }

        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if (type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if (type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if (type.equals(Character.class) || type.equals(Character.TYPE)) {
            return origin.toString().charAt(0);
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
        } else if (type.equals(String.class)) {
            return origin.toString();
        }

        return null;
    }

    @Nullable
    public static Object castMethodResult(Method method, Object data, RpcResponse rpcResponse) {
        if (data instanceof JSONObject) {
            JSONObject jsonResult = (JSONObject) rpcResponse.getData();
            return jsonResult.toJavaObject(method.getReturnType());
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
            Class<?> componentType = method.getReturnType().getComponentType();
            Object resultArray = Array.newInstance(componentType, array.length);
            for (int i = 0; i < array.length; i++) {
                Array.set(resultArray, i, array[i]);
            }

            return resultArray;
        }
        else {
            return TypeUtils.cast(data, method.getReturnType());
        }
    }
}
