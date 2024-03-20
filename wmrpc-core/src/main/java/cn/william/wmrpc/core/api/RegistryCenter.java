package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.registry.ChangedListener;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/19
 */
public interface RegistryCenter {

    void start();

    void stop();

    void register(String service, String instance);

    void unregister(String service, String instance);

    List<String> fetchAll(String service);

    // consumer 订阅
    void subscribe(String service, ChangedListener listener);

    static class StaticRegistryCenter implements RegistryCenter {

        List<String> providers;

        public StaticRegistryCenter(List<String> providers) {
            this.providers = providers;
        }

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
            return providers;
        }

        @Override
        public void subscribe(String service, ChangedListener listener) {

        }
    }
}
