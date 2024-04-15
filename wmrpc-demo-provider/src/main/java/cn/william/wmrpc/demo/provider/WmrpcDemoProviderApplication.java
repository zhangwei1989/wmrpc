package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.api.RpcException;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.config.ProviderConfig;
import cn.william.wmrpc.core.config.ProviderConfigProperty;
import cn.william.wmrpc.core.transport.SpringHttpTransport;
import cn.william.wmrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class WmrpcDemoProviderApplication {

    @Autowired
    ProviderConfigProperty providerConfigProperty;

    @Autowired
    UserService userService;

    @Autowired
    SpringHttpTransport transport;

    @RequestMapping("/ports")
    public RpcResponse ports(@RequestParam("ports") String ports) {
        userService.setPorts(ports);
        RpcResponse response = new RpcResponse();
        response.setStatus(true);
        response.setData("OK,ports set to " + ports);
        return response;
    }

    @RequestMapping("/meta")
    public RpcResponse meta() {
        RpcResponse response = new RpcResponse();
        response.setData(providerConfigProperty.getMetas());

        return response;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
//            // test 1 parameter method
            RpcRequest request = new RpcRequest();
            request.setService("cn.william.wmrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});
//
//            RpcResponse response = transport.invoke(request);
//            System.out.println("response: " + response.getData());
//
//            // test 2 parameters method
//            RpcRequest request1 = new RpcRequest();
//            request1.setService("cn.william.wmrpc.demo.api.UserService");
//            request1.setMethodSign("findById@2_int_java.lang.String");
//            request1.setArgs(new Object[]{100, "CC"});
//
//            RpcResponse response1 = transport.invoke(request1);
//            System.out.println("response1: " + response1.getData());

            // test 5 for traffic control
            System.out.println("Provider Case 5. >>===[复杂测试：测试流量并发控制===");
            for (int i = 0; i < 60; i++) {
                try {
                    Thread.sleep(1000);
                    RpcResponse r = transport.invoke(request);
                    System.out.println(i + " ***>>> " + r.getData());
                } catch (RpcException e) {
                    // ignore
                    System.out.println(i + " ***>>> " + e.getMessage() + " -> " + e.getErrCode());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoProviderApplication.class, args);
    }
}
