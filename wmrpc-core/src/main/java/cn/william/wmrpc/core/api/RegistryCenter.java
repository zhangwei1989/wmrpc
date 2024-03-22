package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.meta.ServiceMeta;
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

    void register(ServiceMeta service, String instance);

    void unregister(ServiceMeta service, String instance);

    List<String> fetchAll(ServiceMeta service);

    // consumer 订阅
    void subscribe(ServiceMeta service, ChangedListener listener);

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
        public void register(ServiceMeta service, String instance) {
        }

        @Override
        public void unregister(ServiceMeta service, String instance) {
        }

        @Override
        public List<String> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }
    }
}
