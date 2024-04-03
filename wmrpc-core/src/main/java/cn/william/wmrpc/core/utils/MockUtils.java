package cn.william.wmrpc.core.utils;

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Mock 工具类.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/2
 */
public class MockUtils {

    public static Object mock(Class<?> type) {
        if (type == Integer.class || type == Integer.TYPE) {
            return 1;
        } else if (type == Long.class || type == Long.TYPE) {
            return 1L;
        } else if (type == Short.class || type == Short.TYPE) {
            return 1;
        } else if (type == Boolean.class || type == Boolean.TYPE) {
            return 0;
        } else if (type == Byte.class || type == Byte.TYPE) {
            return 0;
        } else if (type == Character.class || type == Character.TYPE) {
            return '0';
        } else if (type == Double.class || type == Double.TYPE) {
            return 1.1d;
        } else if (type == Float.class || type == Float.TYPE) {
            return 1.1f;
        }

        if (type == String.class) {
            return "this is a mock string";
        }

        // TODO 针对以下类型的 mock
        // Array
        // List
        // Map
        if (type.isAssignableFrom(Array.class)
                || type.isAssignableFrom(List.class)
                || type.isAssignableFrom(Map.class)) {
            return null;
        }

        // 针对 POJO 的 mock
        return mockPOJO(type);
    }

    @SneakyThrows
    private static Object mockPOJO(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Object obj = type.getDeclaredConstructor().newInstance();

        for (Field field : fields) {
            Class fieldClazz = field.getType();
            Object fieldValue = mock(fieldClazz);
            field.setAccessible(true);
            field.set(obj, fieldValue);
        }

        return obj;
    }

}
