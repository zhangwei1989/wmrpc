package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 消费者配置类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.consumer")
public class ConsumerConfigProperties {

    // for ha and governance
    private int retries = 1;

    private int timeout = 1000;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10000;

    private int halfOpenDelay = 60_000;

    private int grayRatio = 0;

}
