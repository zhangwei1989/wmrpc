package cn.william.wmrpc.core.cluster;

import cn.william.wmrpc.core.loadbalance.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public class RandomLoadBalancer implements LoadBalancer<String> {

    Random random = new Random();

    @Override
    public String choose(List<String> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }

        if (providers.size() == 1) {
            return providers.get(0);
        }

        return providers.get(random.nextInt(providers.size()));
    }
}
