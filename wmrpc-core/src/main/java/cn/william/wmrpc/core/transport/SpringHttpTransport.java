package cn.william.wmrpc.core.transport;

import cn.william.wmrpc.core.api.RpcRequest;
import cn.william.wmrpc.core.api.RpcResponse;
import cn.william.wmrpc.core.provider.ProviderInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务端 HTTP transport
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/15
 */
@RestController
public class SpringHttpTransport {

    @Autowired
    ProviderInvoker providerInvoker;

    // 使用 HTTP + JSON 来实现网络通信和序列化
    @RequestMapping("/wmrpc")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

}
