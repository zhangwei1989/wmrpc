package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.api.RpcException;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.config.ApolloChangedListener;
import cn.william.wmrpc.core.config.ProviderConfig;
import cn.william.wmrpc.core.config.ProviderConfigProperties;
import cn.william.wmrpc.core.transport.SpringBootTransport;
import cn.william.wmrpc.demo.api.UserService;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Import(ProviderConfig.class)
@RestController
@EnableApolloConfig
public class WmrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmrpcDemoProviderApplication.class, args);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Autowired
    UserService userService;

    @Autowired
    SpringBootTransport transport;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @RequestMapping("/ports")
    public RpcResponse<String> ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPorts(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK: " + ports);
        return response;
    }

    @RequestMapping("/meta")
    public String meta() {
        return providerConfigProperties.getMetas().toString();
    }

    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            providerConfigProperties.getMetas().forEach((k, v) -> {
                System.out.println("ProviderConfigProperties: " + k + " = " + v);
            });

            testAll();
        };
    }

    private void testAll() {
//        //  test 1 parameter method
//        System.out.println("Provider Case 1. >>===[基本测试：1个参数]===");
        RpcRequest request = new RpcRequest();
        request.setService("cn.william.wmrpc.demo.api.UserService");
        request.setMethodSign("findById@1_int");
        request.setArgs(new Object[]{100});
//
//        RpcResponse<Object> rpcResponse = transport.invoke(request);
//        System.out.println("return : " + rpcResponse.getData());
//
//        // test 2 parameters method
//        System.out.println("Provider Case 2. >>===[基本测试：2个参数]===");
//        RpcRequest request1 = new RpcRequest();
//        request1.setService("cn.william.wmrpc.demo.api.UserService");
//        request1.setMethodSign("findById@2_int_java.lang.String");
//        request1.setArgs(new Object[]{100, "CC"});
//
//        RpcResponse<Object> rpcResponse1 = transport.invoke(request1);
//        System.out.println("return : " + rpcResponse1.getData());
//
//        // test 3 for List<User> method&parameter
//        System.out.println("Provider Case 3. >>===[复杂测试：参数类型为List<User>]===");
//        RpcRequest request3 = new RpcRequest();
//        request3.setService("cn.william.wmrpc.demo.api.UserService");
//        request3.setMethodSign("getList@1_java.util.List");
//        List<User> userList = new ArrayList<>();
//        userList.add(new User(100, "KK100"));
//        userList.add(new User(101, "KK101"));
//        request3.setArgs(new Object[]{userList});
//        RpcResponse<Object> rpcResponse3 = transport.invoke(request3);
//        System.out.println("return : " + rpcResponse3.getData());
//
//        // test 4 for Map<String, User> method&parameter
//        System.out.println("Provider Case 4. >>===[复杂测试：参数类型为Map<String, User>]===");
//        RpcRequest request4 = new RpcRequest();
//        request4.setService("cn.william.wmrpc.demo.api.UserService");
//        request4.setMethodSign("getMap@1_java.util.Map");
//        Map<String, User> userMap = new HashMap<>();
//        userMap.put("P100", new User(100, "KK100"));
//        userMap.put("P101", new User(101, "KK101"));
//        request4.setArgs(new Object[]{userMap});
//        RpcResponse<Object> rpcResponse4 = transport.invoke(request4);
//        System.out.println("return : " + rpcResponse4.getData());

        // test 5 for traffic control
        System.out.println("Provider Case 5. >>===[复杂测试：测试流量并发控制===");
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(1000);
                RpcResponse<Object> r = transport.invoke(request);
                System.out.println(i + " ***>>> " + r.getData());
            } catch (RpcException e) {
                // ignore
                System.out.println(i + " ***>>> " + e.getMessage() + " -> " + e.getErrcode());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
