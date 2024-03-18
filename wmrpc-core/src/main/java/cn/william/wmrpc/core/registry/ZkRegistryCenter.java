package cn.william.wmrpc.core.registry;

import cn.william.wmrpc.core.api.RegistryCenter;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public class ZkRegistryCenter implements RegistryCenter {

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void register(String service, String instance) {

    }

    @Override
    public void unregister(String service, String instance) {

    }

    @Override
    public List<String> fetchAll(String service) {
        return null;
    }
}
