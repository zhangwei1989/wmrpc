package cn.william.wmrpc.core.config;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.provider.ProviderBootstrap;
import cn.william.wmrpc.core.provider.ProviderInvoker;
import cn.william.wmrpc.core.registry.zw.ZwRegistryCenter;
import cn.william.wmrpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8080}")
    private String port;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean // (initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter provider_rc() {
        return new ZwRegistryCenter();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("providerBootstrap starting...");
            providerBootstrap.start();
            log.info("providerBootstrap started...");
        };
    }
}
