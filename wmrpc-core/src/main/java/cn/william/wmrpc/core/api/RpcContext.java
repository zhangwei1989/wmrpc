package cn.william.wmrpc.core.api;

import lombok.Data;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
@Data
public class RpcContext {

    List<Filter> filters; // TODO

    Router router;

    LoadBalancer loadBalancer;

}
