package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.consumer.ConsumerConfig;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
@Import(ConsumerConfig.class)
@Slf4j
public class WmrpcDemoConsumerApplication {

    @WmConsumer
    private UserService userService;

//    @WmConsumer
//    private OrderService orderService;

    @RequestMapping("/")
    public User findById(@RequestParam("id") int id) {
        return userService.findById(id);
    }

    @RequestMapping("/find/")
    public User find(@RequestParam("timeout") int timeout) {
        long start = System.currentTimeMillis();
        User user = userService.find(timeout);
        log.info("======> userService.find(timeout) cost: {}ms", System.currentTimeMillis() - start);
        return user;
    }

    @Bean
    ApplicationRunner consumerRun() {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
//                log.warn("=============> userService.getId(10f);, the result is {}", userService.getId(10f));
//
//                log.warn("=============> userService.getId(1000);, the result is {}", userService.getId(1000));
//
//                log.warn("=============> userService.getId(new User(100, \"WM\");, the result is {}", userService.getId(new User(100, "WM")));
//                User user = userService.findById(1);
//                System.out.println("RPC result userService.findById(1) = " + user);
//
//                Order order = orderService.findById(2);
//                System.out.println("RPC result orderService.findById(2) = " + order);
//
////                Order order404 = orderService.findById(404);
////                System.out.println("RPC result orderService.findById(404) " + order404);
//
//                // 验证基本调用
//                log.warn("=============> userService.findById(100), the result is {}", userService.findById(100));
//                // 验证重载方法
//                log.warn("=============> userService.findById(100, \"WM\"), the result is {}", userService.findById(100, "WM"));
//                // 验证捕获 provider 异常
////                log.info("=============> userService.findById(404), the result is {}", userService.findById(404));
//                // 验证基本类型
//                log.info("=============> userService.getName(), the result is {}", userService.getName());
//                // 验证方法重载
//                log.info("=============> userService.getName(1), the result is {}", userService.getName(123));
//                // 验证方法重载
//                log.info("=============> userService.getId(1), the result is {}", userService.getId(1));
//                // 验证方法返回数组
//                log.info("=============> userService.getIds(), the result is {}", Arrays.toString(userService.getIds()));
//                // 验证方法返回数组
//                log.info("=============> userService.getLongIds(), the result is {}", Arrays.toString(userService.getLongIds()));
//                // 验证方法参数是数组
//                log.info("=============> userService.getIds(new int[]{4,5,6}), the result is {}", Arrays.toString(userService.getIds(new int[]{4, 5, 6})));
                // 参数和返回值里，map 里面有数组，数组里面有 User
                User user1 = new User(1, "zw");
                User user2 = new User(2, "wcl");
                User user3 = new User(3, "zja");

                List<User> userList = new ArrayList<>();
                userList.add(user1);
                userList.add(user2);
                userList.add(user3);

                Map<String, List> map = new HashMap<>();

                map.put("userMap", userList);
                log.info("=============> userService.getIds(new int[]{4,5,6}), the result is {}", userService.getMutipleUser(map));
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoConsumerApplication.class, args);
    }
}
