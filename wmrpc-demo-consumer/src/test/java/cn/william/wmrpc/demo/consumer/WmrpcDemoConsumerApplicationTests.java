package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.demo.provider.WmrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class WmrpcDemoConsumerApplicationTests {

    private static ApplicationContext context;

    @BeforeAll
    static void init(){
        context = SpringApplication.run(WmrpcDemoProviderApplication.class, "--server.port=8084");
    }

    @Test
    void contextLoads() {
    }

    @AfterAll
    static void stop() {
        SpringApplication.exit(context);
    }

}
