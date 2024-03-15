package cn.william.wmrpc.core.meta;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/15
 */
@Data
public class ProviderMeta {

    Method method;

    String methodSign;

    Object serviceImpl;
}
