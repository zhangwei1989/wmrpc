package cn.william.wmrpc.core.cluster;

import cn.william.wmrpc.core.loadbalance.LoadBalancer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public class RoundRibbonLoadBalancer implements LoadBalancer<String> {

    private AtomicInteger index = new AtomicInteger();

    @Override
    public String choose(List<String> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get((index.getAndIncrement()&0x7fffffff) % providers.size());
    }
}
