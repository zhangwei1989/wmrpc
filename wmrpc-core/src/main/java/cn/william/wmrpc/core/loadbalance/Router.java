package cn.william.wmrpc.core.loadbalance;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public interface Router<T> {

    List<T> route(List<T> providers);

    Router Default = ps -> ps;

}
