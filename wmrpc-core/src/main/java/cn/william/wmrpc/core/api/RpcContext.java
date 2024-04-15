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

    private static ThreadLocal<Map<String, String>> contextParamsHolder =
            ThreadLocal.withInitial(() -> new HashMap<>());

    public static Map<String, String> getContextParams() {
        return contextParamsHolder.get();
    }

    public static String getContextParam(String key) {
        return contextParamsHolder.get().get(key);
    }

    public static Map<String, String> setContextParams(String key, String value) {
        contextParamsHolder.get().putIfAbsent(key, value);
        return getContextParams();
    }

    public static void removeContextParams() {
        contextParamsHolder.get().clear();
    }

}
