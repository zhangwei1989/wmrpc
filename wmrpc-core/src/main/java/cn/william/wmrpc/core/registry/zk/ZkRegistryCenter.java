package cn.william.wmrpc.core.registry.zk;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.api.RpcException;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import cn.william.wmrpc.core.registry.ChangedListener;
import cn.william.wmrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * zk 注册中心
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/18
 */
@Slf4j
public class ZkRegistryCenter implements RegistryCenter {

    private CuratorFramework client = null;

    @Value("${wmrpc.zk.server}")
    String servers;

    @Value("${wmrpc.zk.root}")
    String root;

    private boolean running = false;

    @Override
    public void start() {
        if(running) {
            log.info(" ===> zk client has started to server[" + servers + "/" + root + "], ignored.");
            return;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        log.info("ZK client starting to server[" + servers + "/" + root + "].");
        client.start();
    }

    @Override
    public void stop() {
        if(!running) {
            log.info(" ===> zk client isn't running to server[" + servers + "/" + root + "], ignored.");
            return;
        }
        log.info("ZK client stopping");
        client.close();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instanceMeta) {
        String servicePath = "/" + service.toPath();
        try {
            // 创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, service.toMetas().getBytes());
            }
            // 创建实例的临时性节点
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            log.info("======> register to zk: {}", instancePath);
            if (client.checkExists().forPath(instancePath) == null) {
                client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath, instanceMeta.toMetas().getBytes());
            }
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instanceMeta) {
        String servicePath = "/" + service.toPath();
        try {
            // 判断服务是否存在
            if (client.checkExists().forPath(servicePath) == null) {
                return;
            }
            // 删除实例节点
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            log.info("======> delete from zk: {}", instancePath);
            client.delete().quietly().forPath(instancePath);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        String servicePath = "/" + service.toPath();
        try {
            // 获取所有子节点
            List<String> nodes = client.getChildren().forPath(servicePath);
            log.info("======> fetchAll from zk: {}", servicePath);
            nodes.forEach(System.out::println);

            List<InstanceMeta> providers = nodes.stream().map(x -> {
                String[] strings = x.split("_");
                InstanceMeta instance = InstanceMeta.http(strings[0], strings[1]);

                // 获取 instance Metas 属性
                String nodePath = servicePath + "/" + x;
                byte[] bytes;
                try {
                    bytes = client.getData().forPath(nodePath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Map<String ,String> metas = JSON.parseObject(new String(bytes), HashMap.class);
                System.out.println("instance: " + instance);
                metas.forEach((k, v) -> System.out.println( k + "->" + v));

                instance.setParameters(metas);
                return instance;
            }).collect(Collectors.toList());
            return providers;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        final TreeCache cache = TreeCache.newBuilder(client, "/" + service.toPath())
                .setCacheData(true)
                .setMaxDepth(2)
                .build();

        cache.getListenable().addListener(
                (curator, event) -> {
                    // 有任何节点变动，这里会执行
                    log.info("zk subscribe event: {}", event);
                    List<InstanceMeta> nodes = fetchAll(service);
                    listener.fire(new Event(nodes));
                }
        );

        cache.start();
    }
}
