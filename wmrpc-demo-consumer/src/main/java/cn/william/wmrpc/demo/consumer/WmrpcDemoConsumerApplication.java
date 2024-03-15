package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.consumer.ConsumerConfig;
import cn.william.wmrpc.demo.api.Order;
import cn.william.wmrpc.demo.api.OrderService;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
@SpringBootApplication
@Import({ConsumerConfig.class})
@Slf4j
public class WmrpcDemoConsumerApplication {

    @WmConsumer
    UserService userService;

    @WmConsumer
    OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoConsumerApplication.class, args);
    }

    @Bean
     public ApplicationRunner consumer_runner() {
        return x -> {
//            User user = userService.findById(1);
//            System.out.println("RPC result userService.findById(1) = " + user);

            // 解决让这个不报错
//            userService.getId(1000);
//            userService.getName();

//            Order order = orderService.findById(2);
//            System.out.println("RPC result orderService.findById(2) = " + order);

//            Order order404 = orderService.findById(404);
//            System.out.println("RPC result orderService.findById(404) " + order404);

            // 验证基本调用
            log.info("=============> userService.findById(100), the result is {}", userService.findById(100));
            // 验证重载方法
            log.info("=============> userService.findById(100, \"WM\"), the result is {}", userService.findById(100, "WM"));
            // 验证捕获 provider 异常
//                log.info("=============> userService.findById(404), the result is {}", userService.findById(404));
            // 验证基本类型
            log.info("=============> userService.getName(), the result is {}", userService.getName());
            // 验证方法重载
            log.info("=============> userService.getName(1), the result is {}", userService.getName(123));
            // 验证方法重载
            log.info("=============> userService.getId(1), the result is {}", userService.getId(1));
        };
    }
}
