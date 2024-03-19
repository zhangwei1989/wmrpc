package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.loadbalance.LoadBalancer;
import cn.william.wmrpc.core.loadbalance.Router;
import lombok.Getter;
import okhttp3.Route;

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
