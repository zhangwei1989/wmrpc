package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
@Getter
public class RpcContext {

    private Router<InstanceMeta> router;

    private LoadBalancer<InstanceMeta> loadBalancer;

    private Map<String, String> parameters = new HashMap<>();

    public RpcContext(Router router, LoadBalancer loadBalancer) {
        this.router = router;
        this.loadBalancer = loadBalancer;
    }
}
