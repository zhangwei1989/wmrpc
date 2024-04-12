package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 服务端配之类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.provider")
public class ProviderConfigProperty {

    private Map<String, String> metas;

}
