package cn.william.wmrpc.core.api;

import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import cn.william.wmrpc.core.registry.ChangedListener;

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
    void register(ServiceMeta service, InstanceMeta instanceMeta);

    // provider 侧
    void unregister(ServiceMeta service, InstanceMeta instanceMeta);

    // consumer 侧
    List<InstanceMeta> fetchAll(ServiceMeta service);

    void subscribe(ServiceMeta service, ChangedListener listener);

    //void subscribe();

    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instanceMeta) {

        }

        @Override
        public void unregister(ServiceMeta service, InstanceMeta instanceMeta) {

        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }
    }

}
