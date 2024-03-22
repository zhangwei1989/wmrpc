package cn.william.wmrpc.core.consumer;

import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.client.OkHttpInvoker;
import cn.william.wmrpc.core.utils.MethodUtils;
import cn.william.wmrpc.core.utils.TypeUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/13
 */
@Slf4j
public class WmConsumerInvocationHandler implements InvocationHandler {

//    private static ApplicationContext applicationContext;

    private RpcContext rpcContext;

    private final String serviceName;

    private List<String> providers;

    private OkHttpInvoker okHttpInvoker = new OkHttpInvoker();

    public WmConsumerInvocationHandler(String serviceName, RpcContext rpcContext, List<String> providers) {
        /*if (WmConsumerInvocationHandler.applicationContext == null) {
            WmConsumerInvocationHandler.applicationContext = new AnnotationConfigApplicationContext();
        }*/
        this.serviceName = serviceName;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        okHttpInvoker = WmConsumerInvocationHandler.applicationContext.getBean(OkHttpInvoker.class);
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(this.serviceName);
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setArgs(args);

        List<String> ps = rpcContext.getRouter().route(providers);
        String url = (String) rpcContext.getLoadBalancer().choose(ps);

        log.info("==========> loadbalance choose url is {}", url);

        RpcResponse rpcResponse = okHttpInvoker.post(rpcRequest, url);

        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getException();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
