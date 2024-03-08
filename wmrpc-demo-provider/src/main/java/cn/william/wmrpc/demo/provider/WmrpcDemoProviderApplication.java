package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.provider.ProviderBootstrap;
import cn.william.wmrpc.core.provider.ProviderConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
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
@Import(ProviderConfig.class)
public class WmrpcDemoProviderApplication {

    @Autowired
    ProviderBootstrap providerBootstrap;

    // 使用 HTTP + JSON 来实现网络通信和序列化
    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("cn.william.wmrpc.demo.api.UserService");
            request.setMethod("findById");
            request.setArgs(new Object[]{100});

            RpcResponse response = providerBootstrap.invoke(request);
            System.out.printf("response: " + response.getData());
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoProviderApplication.class, args);
    }
}
