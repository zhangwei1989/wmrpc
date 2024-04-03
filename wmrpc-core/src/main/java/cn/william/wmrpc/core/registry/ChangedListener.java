package cn.william.wmrpc.core.registry;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/20
 */
public interface ChangedListener {

    void fire(Event event);
}
