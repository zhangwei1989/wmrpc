package cn.william.wmrpc.core.registry;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
public interface ChangedListener {

    void fire(Event event);
}
