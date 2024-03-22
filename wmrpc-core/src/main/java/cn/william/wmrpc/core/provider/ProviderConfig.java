package cn.william.wmrpc.core.provider;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.consumer.ConsumerBootstrap;
import cn.william.wmrpc.core.registry.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
@Slf4j
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
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

    @Bean // (initMethod = "start", destroyMethod = "stop")
    RegistryCenter provider_rc() {
        return new ZkRegistryCenter();
    }
}
