package cn.william.wmrpc.core.api;

import lombok.Getter;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
@Getter
public class RpcContext {

    private Router router;

    private LoadBalancer loadBalancer;

    public RpcContext(Router router, LoadBalancer loadBalancer) {
        this.router = router;
        this.loadBalancer = loadBalancer;
    }
}
