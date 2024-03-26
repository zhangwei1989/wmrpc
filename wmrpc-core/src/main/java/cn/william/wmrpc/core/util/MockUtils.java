package cn.william.wmrpc.core.util;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * Mock 工具类
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/23
 */
public class MockUtils {

    public static Object mock(Class type) {
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return 1;
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return 10000L;
        }

        if (Number.class.isAssignableFrom(type)) {
            return 1;
        }

        if (type.equals(String.class)) {
            return "this_is_a_mock_string";
        }

        return mockPojo(type);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fType = field.getType();
            Object fValue = mock(fType);
            field.set(result, fValue);
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
    }

    public static class UserDto {

        private int a;

        private String b;

        @Override
        public String toString() {
            return a + "," +b;
        }

    }
}
