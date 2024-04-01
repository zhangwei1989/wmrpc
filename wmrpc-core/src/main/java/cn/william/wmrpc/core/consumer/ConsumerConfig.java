package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.Filter;
import cn.william.wmrpc.core.api.LoadBalancer;
import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.cluster.RandomLoadBalancer;
import cn.william.wmrpc.core.cluster.RoundRibonLoadBalancer;
import cn.william.wmrpc.core.filter.CacheFilter;
import cn.william.wmrpc.core.filter.MockFilter;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 消费端配置类
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
@Configuration
public class ConsumerConfig {

    @Value("${wmrpc.providers}")
    String servers;

    @Bean
    public ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumeBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
//        return LoadBalancer.Default;
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> loadRouter() {
        return Router.Default;
    }

//    @Bean
//    public Filter loadFilter() {
//        return Filter.Default;
//    }

//    @Bean
//    public Filter loadFilter() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter loadFilter() {
//        return new MockFilter();
//    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }
}
