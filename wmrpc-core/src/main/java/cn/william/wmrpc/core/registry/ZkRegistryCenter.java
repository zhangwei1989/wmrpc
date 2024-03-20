package cn.william.wmrpc.core.registry;

import cn.william.wmrpc.core.api.ChangedListener;
import cn.william.wmrpc.core.api.RegistryCenter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client;

    @Override
    public void start() {
        // 创建 ZK 客户端
        client = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("wmrpc-rc")
                .retryPolicy(new BoundedExponentialBackoffRetry(1000, 1000, 3))
                .build();
        client.start();
    }

    @Override
    public void stop() {
        client.close();
    }

    @SneakyThrows
    @Override
    public void register(String service, String instance) {
        String servicePath = "/" + service;
        // 创建持久化存储
        if (client.checkExists().forPath(servicePath) == null) {
            client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath);
        }

        // 创建临时节点
        String instancePath = servicePath + "/" + instance;
        if (client.checkExists().forPath(instancePath) == null) {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath);
        }
    }

    @SneakyThrows
    @Override
    public void unregister(String service, String instance) {
        String servicePath = "/" + service;
        // 创建持久化存储
        if (client.checkExists().forPath(servicePath) == null) {
            return;
        }

        // 创建临时节点
        String instancePath = servicePath + "/" + instance;
        client.delete().forPath(instancePath);
    }

    @SneakyThrows
    @Override
    public List<String> fetchAll(String service) {
        String servicePath = "/" + service;
        List<String> nodes = client.getChildren().forPath(servicePath);

        return nodes;
    }

    // consumer 订阅
    @Override
    public void subscribe(ChangedListener listener) {

    }

}