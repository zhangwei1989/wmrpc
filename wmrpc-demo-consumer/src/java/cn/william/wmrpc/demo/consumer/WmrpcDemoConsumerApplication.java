package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.annotation.WmConsumer;
import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.consumer.ConsumerConfig;
import cn.william.wmrpc.core.provider.ProviderBootstrap;
import cn.william.wmrpc.core.provider.ProviderConfig;
import cn.william.wmrpc.demo.api.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@Import(ConsumerConfig.class)
@Slf4j
public class WmrpcDemoConsumerApplication {

    @WmConsumer
    private UserService userService;

    @Bean
    ApplicationRunner consumerRun() {
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                // 验证基本调用
                log.info("=============> userService.findById(100), the result is {}", userService.findById(100));
                // 验证捕获 provider 异常
//                log.info("=============> userService.findById(404), the result is {}", userService.findById(404));
                // 验证基本类型
                log.info("=============> userService.getId(1), the result is {}", userService.getId(1));
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoConsumerApplication.class, args);
    }
}
