package cn.william.wmrpc.core.cluster;

import cn.william.wmrpc.core.api.LoadBalancer;
import cn.william.wmrpc.core.meta.InstanceMeta;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/18
 */
public class RoundRibbonLoadBalancer implements LoadBalancer<InstanceMeta> {

    private AtomicInteger index = new AtomicInteger();

    @Override
    public InstanceMeta choose(List<InstanceMeta> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get((index.getAndIncrement()&0x7fffffff) % providers.size());
    }
}
