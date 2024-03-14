package cn.william.wmrpc.core.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    // 保证 Spring 容器启动后，在 ApplicationRunner 中第一个执行
    @Order(Integer.MIN_VALUE)
    ApplicationRunner consumerRunStart(ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("ConsumerBootstrap start...");
            consumerBootstrap.start();
            log.info("ConsumerBootstrap started...");
        };
    }
}
