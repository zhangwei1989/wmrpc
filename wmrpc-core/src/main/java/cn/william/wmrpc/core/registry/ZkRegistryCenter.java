package cn.william.wmrpc.core.registry;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client;

    @Value("${wmrpc.zk.url}")
    private String url;

    @Value("${wmrpc.zk.namespace}")
    private String namespace;

    @Override
    public void start() {
        // 创建 ZK 客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(url)
                .namespace(namespace)
                .retryPolicy(new BoundedExponentialBackoffRetry(1000, 1000, 3))
                .build();
        client.start();
        log.info("======> zk rc started......");
    }

    @Override
    public void stop() {
        client.close();
        log.info("======> zk rc stoped......");
    }

    @SneakyThrows
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        // 创建持久化存储
        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
            log.info("======> service - {} registered to zk rc.", servicePath);
        }

        // 创建临时节点
        String instancePath = servicePath + "/" + instance.toPath();
        if (client.checkExists().forPath(instancePath) == null) {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
            log.info("======> instance - {} registered to zk rc.", instancePath);
        }
    }

    @SneakyThrows
    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        String servicePath = "/" + service.toPath();
        // 创建持久化存储
        if (client.checkExists().forPath(servicePath) == null) {
            return;
        }

        // 创建临时节点
        String instancePath = servicePath + "/" + instance.toPath();
        client.delete().quietly().forPath(instancePath);
        log.info("======> instance - {} unregistered from zk rc.", instancePath);
    }

    @SneakyThrows
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        List<String> nodes = client.getChildren().forPath(servicePath);

        return nodes.stream().map(n ->
                InstanceMeta.builder().scheme("http").host(n.split("_")[0]).port(Integer.valueOf(n.split("_")[1])).context(n.split("_")[2]).build()).collect(Collectors.toList());
    }

    // consumer 订阅
    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        String servicePath = "/" + service.toPath();
        final TreeCache cache = TreeCache.newBuilder(client, servicePath)
                .setMaxDepth(2)
                .setCacheData(true)
                .build();
        cache.start();
        cache.getListenable().addListener((curatorFramework, treeCacheEvent) -> {
            // 有任何变动，这里会执行
            log.info("======> TreeCache Event is {}", treeCacheEvent);
            List<InstanceMeta> nodes = fetchAll(service);
            listener.fire(new Event(nodes));
        });
    }

}
