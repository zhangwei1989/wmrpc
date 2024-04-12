package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者配置属性类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.consumer")
public class ConsumerConfigProperty {

    private Integer retries = 2;

    private Integer timeout = 1000;

    private Integer grayRatio = 33;

    private Integer faultLimit = 10;

    private Integer halfOpenInitialDelay = 1000;

    private Integer halfOpenDelay = 60;

}
