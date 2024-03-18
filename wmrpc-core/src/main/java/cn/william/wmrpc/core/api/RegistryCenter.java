package cn.william.wmrpc.core.api;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
public interface RegistryCenter {

    void start();

    void stop();

    // provider 侧
    void register(String service, String instance);

    // provider 侧
    void unregister(String service, String instance);

    // consumer 侧
    List<String> fetchAll(String service);

    //void subscribe();

    class StaticRegistryCenter implements RegistryCenter {

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
    }

}
