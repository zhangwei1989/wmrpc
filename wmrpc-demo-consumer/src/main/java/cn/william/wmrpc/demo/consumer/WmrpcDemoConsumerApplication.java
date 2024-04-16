package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.cluster.GrayRouter;
import cn.william.wmrpc.core.config.ConsumerConfig;
import cn.william.wmrpc.core.config.ConsumerConfigProperty;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@SpringBootApplication
@RestController
@Slf4j
@Import(ConsumerConfig.class)
public class WmrpcDemoConsumerApplication {

    @WmConsumer
    private UserService userService;

    @Autowired
    private Router router;

    @Autowired
    private ConsumerConfigProperty consumerConfigProperty;

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

    @RequestMapping("/gray")
    public String gray(@RequestParam("ratio") int ratio) {
        ((GrayRouter) router).setGrayRatio(ratio);
        return "OK, gray ratio is set to :" + ratio;
    }

    @RequestMapping("/consumer/configuration")
    public Integer configuration() {
        return consumerConfigProperty.getGrayRatio();
    }

    @Bean
    ApplicationRunner consumerRun() {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) {
                testAll();
            }

            private void testAll() {
                // 常规int类型，返回User对象
                System.out.println("Case 1. >>===[常规int类型，返回User对象]===");
                User user = userService.findById(1);
                System.out.println("RPC result userService.findById(1) = " + user);

                // 测试方法重载，同名方法，参数不同
                System.out.println("Case 2. >>===[测试方法重载，同名方法，参数不同===");
                User user1 = userService.findById(1, "zw");
                System.out.println("RPC result userService.findById(1, \"zw\") = " + user1);

                // 测试返回字符串
                System.out.println("Case 3. >>===[测试返回字符串]===");
                System.out.println("userService.getName() = " + userService.getName());

                // 测试重载方法返回字符串
                System.out.println("Case 4. >>===[测试重载方法返回字符串]===");
                System.out.println("userService.getName(123) = " + userService.getName(123));

                // 测试local toString方法
                System.out.println("Case 5. >>===[测试local toString方法]===");
                System.out.println("userService.toString() = " + userService.toString());

                // 测试long类型
                System.out.println("Case 6. >>===[常规int类型，返回User对象]===");
                System.out.println("userService.getId(10) = " + userService.getId(10));

                // 测试long+float类型
                System.out.println("Case 7. >>===[测试long+float类型]===");
                System.out.println("userService.getId(10f) = " + userService.getId(10f));

                // 测试参数是User类型
                System.out.println("Case 8. >>===[测试参数是User类型]===");
                System.out.println("userService.getId(new User(100,\"KK\")) = " +
                        userService.getId(new User(100, "KK")));

                System.out.println("Case 9. >>===[测试返回long[]]===");
                System.out.println(" ===> userService.getLongIds(): ");
                for (long id : userService.getLongIds()) {
                    System.out.println(id);
                }

                System.out.println("Case 10. >>===[测试参数和返回值都是long[]]===");
                System.out.println(" ===> userService.getLongIds(): ");
                for (long id : userService.getIds(new int[]{4, 5, 6})) {
                    System.out.println(id);
                }

                // 测试参数和返回值都是List类型
                System.out.println("Case 11. >>===[测试参数和返回值都是List类型]===");
                List<User> list = userService.getList(List.of(
                        new User(100, "KK100"),
                        new User(101, "KK101")));
                list.forEach(System.out::println);

                // 测试参数和返回值都是Map类型
                System.out.println("Case 12. >>===[测试参数和返回值都是Map类型]===");
                Map<String, User> map = new HashMap<>();
                map.put("A200", new User(200, "KK200"));
                map.put("A201", new User(201, "KK201"));
                userService.getMap(map).forEach(
                        (k, v) -> System.out.println(k + " -> " + v)
                );

                System.out.println("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
                System.out.println("userService.getFlag(false) = " + userService.getFlag(false));

                System.out.println("Case 14. >>===[测试参数和返回值都是User[]类型]===");
                User[] users = new User[]{
                        new User(100, "KK100"),
                        new User(101, "KK101")};
                Arrays.stream(userService.findUsers(users)).forEach(System.out::println);

                System.out.println("Case 15. >>===[测试参数为long，返回值是User类型]===");
                User userLong = userService.findById(10000L);
                System.out.println(userLong);

                System.out.println("Case 16. >>===[测试参数为boolean，返回值都是User类型]===");
                User user100 = userService.ex(false);
                System.out.println(user100);

                System.out.println("Case 17. >>===[测试服务端抛出一个RuntimeException异常]===");
                try {
                    User userEx = userService.ex(true);
                    System.out.println(userEx);
                } catch (RuntimeException e) {
                    System.out.println(" ===> exception: " + e.getMessage());
                }

                System.out.println("Case 18. >>===[测试服务端抛出一个超时重试后成功的场景]===");
                // 超时设置的【漏斗原则】
                // A 2000 -> B 1500 -> C 1200 -> D 1000
                long start = System.currentTimeMillis();
                userService.find(1100);
                userService.find(1100);
                System.out.println("userService.find take "
                        + (System.currentTimeMillis() - start) + " ms");

                System.out.println("Case 19. >>===[测试通过Context跨消费者和提供者进行传参]===");
                String Key_Version = "rpc.version";
                String Key_Message = "rpc.message";
                RpcContext.setContextParams(Key_Version, "v8");
                RpcContext.setContextParams(Key_Message, "this is a v8 message");
                RpcContext.setContextParams("a", "a100");
                String version = userService.echoParameter(Key_Version);
                RpcContext.setContextParams(Key_Version, "v9");
                RpcContext.setContextParams(Key_Message, "this is a v9 message");
                RpcContext.setContextParams("b", "b200");
                String message = userService.echoParameter(Key_Message);
                System.out.println(" ===> echo parameter from c->p->c: " + Key_Version + " -> " + version);
                System.out.println(" ===> echo parameter from c->p->c: " + Key_Message + " -> " + message);

                // 参数和返回值里，map 里面有数组，数组里面有 User
//                System.out.println("Case 20. >>===[参数和返回值里，map 里面有数组，数组里面有 User]===");
//                User user2 = new User(1, "zw");
//                User user3 = new User(2, "wcl");
//                User user4 = new User(3, "zja");
//
//                List<User> userList = new ArrayList<>();
//                userList.add(user2);
//                userList.add(user3);
//                userList.add(user4);
//
//                Map<String, List<User>> map1 = new HashMap<>();
//
//                map1.put("userMap", userList);
//                log.info("=============> userService.getIds(new int[]{4,5,6}), the result is {}", userService.getMutipleUser(map1));
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoConsumerApplication.class, args);
    }
}
