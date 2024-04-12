package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rpc 上下文
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/18
 */
@Data
@NoArgsConstructor
public class RpcContext {

    private Router<InstanceMeta> router;

    private LoadBalancer<InstanceMeta> loadBalancer;

    @Setter
    private List<RpcFilter> filters;

    private Map<String, String> parameters = new HashMap<>();

}
