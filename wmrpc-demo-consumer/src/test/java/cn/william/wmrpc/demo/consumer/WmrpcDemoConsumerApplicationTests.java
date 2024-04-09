package cn.william.wmrpc.demo.consumer;

import cn.william.wmrpc.core.test.TestZKServer;
import cn.william.wmrpc.demo.provider.WmrpcDemoProviderApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(classes = {WmrpcDemoConsumerApplication.class})
class WmrpcDemoConsumerApplicationTests {

    static ApplicationContext context1;
    static ApplicationContext context2;

    static TestZKServer zkServer = new TestZKServer();

    private static ApplicationContext context;

    @BeforeAll
    static void init(){
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============     ZK2182    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        zkServer.start();
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8094    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");

        context1 = SpringApplication.run(WmrpcDemoProviderApplication.class,
                "--server.port=8094",
                "--wmrpc.zk.server=localhost:2182",
                "--wmrpc.app.env=test",
                "--logging.level.cn.william.wmrpc=info",
                "--wmrpc.provider.metas.dc=bj",
                "--wmrpc.provider.metas.gray=false",
                "--wmrpc.provider.metas.unit=B001"
        );

        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        System.out.println(" =============      P8095    ========== ");
        System.out.println(" ====================================== ");
        System.out.println(" ====================================== ");
        context2 = SpringApplication.run(WmrpcDemoProviderApplication.class,
                "--server.port=8095",
                "--wmrpc.zk.server=localhost:2182",
                "--wmrpc.app.env=test",
                "--logging.level.cn.william.wmrpc=info",
                "--wmrpc.provider.metas.dc=bj",
                "--wmrpc.provider.metas.gray=false",
                "--wmrpc.provider.metas.unit=B002"
        );
    }

    @Test
    void contextLoads() {
        System.out.println(" ===> aaaa  .... ");
    }

    @AfterAll
    static void stop() {
        SpringApplication.exit(context1, () -> 1);
        SpringApplication.exit(context2, () -> 1);
        zkServer.stop();
    }

}
