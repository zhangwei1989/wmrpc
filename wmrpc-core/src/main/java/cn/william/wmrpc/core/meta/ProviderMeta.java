package cn.william.wmrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/17
 */
@Data
public class ProviderMeta {

    private Object bean;

    private Method method;

}
