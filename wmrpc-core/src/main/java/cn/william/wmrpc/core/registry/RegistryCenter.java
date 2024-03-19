package cn.william.wmrpc.core.registry;

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

    boolean register(String provider);

    boolean unregister(String provider);

    List<String> fetchAll(String service);

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
        public boolean register(String provider) {
            return false;
        }

        @Override
        public boolean unregister(String provider) {
            return false;
        }

        @Override
        public List<String> fetchAll(String service) {
            return providers;
        }
    }

}
