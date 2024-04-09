package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.provider")
public class ProviderConfigProperties {

    Map<String, String> metas = new HashMap<>();

}
