package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.cluster.RoundRibbonLoadBalancer;
import cn.william.wmrpc.core.api.LoadBalancer;
import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.api.RegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    @Autowired
    private Environment environment;

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

    @Bean
    LoadBalancer consumser_lb() {
//        return LoadBalancer.Default;
        return new RoundRibbonLoadBalancer();
    }

    @Bean
    Router consumer_rt() {
        return Router.Default;
    }

    @Bean
    RegistryCenter consumer_rc() {
        List<String> providers = List.of(environment.getProperty("wmrpc.providers").split(","));
        return new RegistryCenter.StaticRegistryCenter(providers);
    }

    @Bean
    RpcContext rpcContext(@Autowired Router router, @Autowired LoadBalancer loadBalancer) {
        return new RpcContext(router, loadBalancer);
    }
}
