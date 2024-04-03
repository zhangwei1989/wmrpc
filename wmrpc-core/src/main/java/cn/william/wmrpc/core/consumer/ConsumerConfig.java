package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.LoadBalancer;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.cluster.GrayRouter;
import cn.william.wmrpc.core.cluster.RoundRibbonLoadBalancer;
import cn.william.wmrpc.core.registry.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 消费者配置类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/13
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    @Value("${wmrpc.grayRatio}")
    private int grayRatio;

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
        return new GrayRouter(grayRatio);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    @Bean
    RpcContext rpcContext(@Autowired Router router, @Autowired LoadBalancer loadBalancer) {
        return new RpcContext(router, loadBalancer);
    }

//    @Bean
//    RpcFilter filter() {
//        return new MockFilter();
//    }

}
