package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * App配置属性类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.app")
public class AppConfigProperty {

    private String id = "wmrpc";

    private String namespace = "public";

    private String env = "dev";

    private String version = "1.0";

}
