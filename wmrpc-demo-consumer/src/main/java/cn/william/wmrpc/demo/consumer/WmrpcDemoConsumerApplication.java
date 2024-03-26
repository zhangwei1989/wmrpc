package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.consumer.ConsumerConfig;
import cn.william.wmrpc.demo.api.Order;
import cn.william.wmrpc.demo.api.OrderService;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/10
 */
@SpringBootApplication
@Import({ConsumerConfig.class})
@RestController
@Slf4j
public class WmrpcDemoConsumerApplication {

    @WmConsumer
    UserService userService;

    @WmConsumer
    OrderService orderService;

    @RequestMapping("/")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoConsumerApplication.class, args);
    }

    @Bean
     public ApplicationRunner consumer_runner() {
        return x -> {
            log.warn("=============> userService.getId(10f);, the result is {}", userService.getId(10f));

            log.warn("=============> userService.getId(1000);, the result is {}", userService.getId(1000));

            log.warn("=============> userService.getId(new User(100, \"WM\");, the result is {}", userService.getId(new User(100, "WM")));
            User user = userService.findById(1);
            System.out.println("RPC result userService.findById(1) = " + user);

            // 解决让这个不报错
            userService.getId(1000);
            userService.getName();

//            Order order = orderService.findById(2);
//            System.out.println("RPC result orderService.findById(2) = " + order);

//            Order order404 = orderService.findById(404);
//            System.out.println("RPC result orderService.findById(404) " + order404);

            // 验证基本调用
            log.info("=============> userService.findById(100), the result is {}", userService.findById(100));
            // 验证重载方法
            log.info("=============> userService.findById(100, \"WM\"), the result is {}", userService.findById(100, "WM"));
//            // 验证捕获 provider 异常
////                log.info("=============> userService.findById(404), the result is {}", userService.findById(404));
            // 验证基本类型
            log.info("=============> userService.getName(), the result is {}", userService.getName());
            // 验证方法重载
            log.info("=============> userService.getName(1), the result is {}", userService.getName(123));
            // 验证方法重载
            log.info("=============> userService.getId(1), the result is {}", userService.getId(1));
            // 验证方法返回数组
//            log.info("=============> userService.getIds(), the result is {}", Arrays.toString(userService.getIds()));
            // 验证方法返回数组
//            log.info("=============> userService.getLongIds(), the result is {}", Arrays.toString(userService.getLongIds()));
            // 验证方法返回数组
//            log.info("=============> userService.getIds(new int[]{4,5,6}), the result is {}", Arrays.toString(userService.getIds(new int[]{4,5,6})));
        };
    }
}
