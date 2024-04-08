package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.consumer")
public class ConsumerConfigProperties {

    // for ha and governance
    private int retries;

    private int timeout;

    private int faultLimit;

    private int halfOpenInitialDelay;

    private int halfOpenDelay;

    private int grayRatio;

}
