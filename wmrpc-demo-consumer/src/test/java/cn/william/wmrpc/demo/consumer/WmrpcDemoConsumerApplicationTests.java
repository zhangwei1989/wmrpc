package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.demo.zk.TestZKServer;
import cn.william.wmrpc.demo.provider.WmrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class WmrpcDemoConsumerApplicationTests {

    private static ConfigurableApplicationContext context;

    static TestZKServer zkServer = new TestZKServer();

    @BeforeAll
    public static void init() {
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");

        zkServer.start();

        context = SpringApplication.run(WmrpcDemoProviderApplication.class,
                "--server.port=8084", "--wmrpc.zk.url=localhost:2182");
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> aaaa  .... ");
    }

    @AfterAll
    public static void stop() {
        SpringApplication.exit(context);
        zkServer.stop();
    }

}
