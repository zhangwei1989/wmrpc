package cn.william.wmrpc.core.annotation;

import cn.william.wmrpc.core.config.ConsumerConfig;
import cn.william.wmrpc.core.config.ProviderConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 组合入口
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableWmrpc {
}
