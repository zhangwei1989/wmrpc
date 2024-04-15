package cn.william.wmrpc.core.config;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.cluster.GrayRouter;
import cn.william.wmrpc.core.cluster.RoundRibbonLoadBalancer;
import cn.william.wmrpc.core.consumer.ConsumerBootstrap;
import cn.william.wmrpc.core.filter.ParamsFilter;
import cn.william.wmrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * 消费者配置类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/13
 */
@Configuration
@Slf4j
@Import({AppConfigProperty.class, ConsumerConfigProperty.class, ZkConfigProperty.class})
public class ConsumerConfig {

    @Autowired
    private AppConfigProperty appConfigProperty;

    @Autowired
    private ConsumerConfigProperty consumerConfigProperty;

    @Autowired
    private ZkConfigProperty zkConfigProperty;

    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap(appConfigProperty, consumerConfigProperty);
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
        return new GrayRouter(consumerConfigProperty.getGrayRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    RegistryCenter consumer_rc() {
        return new ZkRegistryCenter(zkConfigProperty);
    }

    @Bean
    RpcContext rpcContext(@Autowired Router router, @Autowired LoadBalancer loadBalancer) {
        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);
        rpcContext.getParameters().put("wmrpc.retires", String.valueOf(consumerConfigProperty.getRetries()));
        rpcContext.getParameters().put("wmrpc.timeout", String.valueOf(consumerConfigProperty.getTimeout()));
        rpcContext.getParameters().put("wmrpc.grayRatio", String.valueOf(consumerConfigProperty.getGrayRatio()));
        rpcContext.getParameters().put("wmrpc.faultLimit", String.valueOf(consumerConfigProperty.getFaultLimit()));
        rpcContext.getParameters().put("wmrpc.halfOpenInitialDelay", String.valueOf(consumerConfigProperty.getHalfOpenInitialDelay()));
        rpcContext.getParameters().put("wmrpc.halfOpenDelay", String.valueOf(consumerConfigProperty.getHalfOpenDelay()));
        return rpcContext;
    }

    @Bean
    ApolloChangedEventListener apolloChangedEventListener() {
        return new ApolloChangedEventListener();
    }

    @Bean
    RpcFilter filter() {
        return new ParamsFilter();
    }

//    @Bean
//    RpcFilter filter() {
//        return new MockFilter();
//    }

}
