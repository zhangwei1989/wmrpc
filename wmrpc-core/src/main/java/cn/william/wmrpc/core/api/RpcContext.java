package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rpc 上下文
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
@Getter
public class RpcContext {

    private Router<InstanceMeta> router;

    private LoadBalancer<InstanceMeta> loadBalancer;

    @Setter
    private List<RpcFilter> filters;

    private Map<String, String> parameters = new HashMap<>();

    public RpcContext(Router router, LoadBalancer loadBalancer) {
        this.router = router;
        this.loadBalancer = loadBalancer;
    }

}
