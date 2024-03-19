package cn.william.wmrpc.core.loadbalance;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public interface LoadBalancer<T> {

    T choose(List<T> providers);

    LoadBalancer Default = ps -> ps == null || ps.size() == 0 ? null : ps.get(0);

}
