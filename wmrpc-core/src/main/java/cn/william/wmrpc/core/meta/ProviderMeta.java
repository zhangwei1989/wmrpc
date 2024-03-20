package cn.william.wmrpc.core.meta;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 描述 Provider 映射关系
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/15
 */
@Data
@Builder
public class ProviderMeta {

    Method method;

    String methodSign;

    Object serviceImpl;
}
