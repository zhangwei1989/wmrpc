package cn.william.wmrpc.core.config;

import cn.william.wmrpc.core.api.*;
import cn.william.wmrpc.core.cluster.GrayRouter;
import cn.william.wmrpc.core.cluster.RoundRibonLoadBalancer;
import cn.william.wmrpc.core.consumer.ConsumerBootstrap;
import cn.william.wmrpc.core.filter.ParameterFilter;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * 消费端配置类
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
@Slf4j
@Configuration
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ConsumerConfigProperties consumerConfigProperties;

    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumeBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("consumerBootstrap starting ...");
            consumerBootstrap.start();
            log.info("consumerBootstrap started ...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
//        return LoadBalancer.Default;
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router loadRouter() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    @Bean
    public Filter loadFilter() {
        return new ParameterFilter();
    }

//    @Bean
//    public Filter loadFilter() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter loadFilter() {
//        return new MockFilter();
//    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.setAppConfigProperties(appConfigProperties);
        context.setConsumerConfigProperties(consumerConfigProperties);
        return context;
    }
}
