package cn.william.wmrpc.core.config;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.provider.ProviderBootstrap;
import cn.william.wmrpc.core.provider.ProviderInvoker;
import cn.william.wmrpc.core.registry.zw.ZwRegistryCenter;
import cn.william.wmrpc.core.transport.SpringHttpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
@Import({AppConfigProperty.class, ProviderConfigProperty.class, ZkConfigProperty.class, SpringHttpTransport.class})
public class ProviderConfig {

    @Autowired
    private AppConfigProperty appConfigProperty;

    @Autowired
    private ProviderConfigProperty providerConfigProperty;

    @Autowired
    private ZkConfigProperty zkConfigProperty;

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap(appConfigProperty, providerConfigProperty);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    // 保证 Spring 容器启动后，在 ApplicationRunner 中第一个执行
    @Order(Integer.MIN_VALUE)
    ApplicationRunner providerRunStart(ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("ConsumerBootstrap start...");
            providerBootstrap.start();
            log.info("ConsumerBootstrap started...");
        };
    }

    @Bean
        // (initMethod = "start", destroyMethod = "stop")
    RegistryCenter provider_rc() {
        return new ZwRegistryCenter();
    }

    @Bean
    ApolloChangedEventListener apolloChangedEventListener() {
        return new ApolloChangedEventListener();
    }

}
