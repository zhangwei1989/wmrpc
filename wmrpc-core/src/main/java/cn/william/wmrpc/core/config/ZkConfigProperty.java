package cn.william.wmrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ZK 注册中心配置类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/12
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wmrpc.zk")
public class ZkConfigProperty {

    private String server = "localhost:2181";

    private String root = "wmrpc-rc";

}
