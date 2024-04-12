package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.config.ProviderConfig;
import cn.william.wmrpc.core.provider.ProviderInvoker;
import cn.william.wmrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class WmrpcDemoProviderApplication {

    @Autowired
    ProviderInvoker providerInvoker;

    @Autowired
    UserService userService;

    // 使用 HTTP + JSON 来实现网络通信和序列化
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @RequestMapping("/ports")
    public RpcResponse ports(@RequestParam("ports") String ports) {
        userService.setPorts(ports);
        RpcResponse response = new RpcResponse();
        response.setStatus(true);
        response.setData("OK,ports set to " + ports);
        return response;
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            // test 1 parameter method
            RpcRequest request = new RpcRequest();
            request.setService("cn.william.wmrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});

            RpcResponse response = providerInvoker.invoke(request);
            System.out.println("response: " + response.getData());

            // test 2 parameters method
            RpcRequest request1 = new RpcRequest();
            request1.setService("cn.william.wmrpc.demo.api.UserService");
            request1.setMethodSign("findById@2_int_java.lang.String");
            request1.setArgs(new Object[]{100, "CC"});

            RpcResponse response1 = providerInvoker.invoke(request1);
            System.out.println("response1: " + response1.getData());
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoProviderApplication.class, args);
    }
}
