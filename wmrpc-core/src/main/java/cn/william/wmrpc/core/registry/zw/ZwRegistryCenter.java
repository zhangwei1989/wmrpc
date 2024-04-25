package cn.william.wmrpc.core.registry.zw;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.consumer.http.HttpInvoker;
import cn.william.wmrpc.core.meta.InstanceMeta;
import cn.william.wmrpc.core.meta.ServiceMeta;
import cn.william.wmrpc.core.registry.ChangedListener;
import cn.william.wmrpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * implementation for zw registry center
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/25
 */
@Slf4j
public class ZwRegistryCenter implements RegistryCenter {

    @Value("${zwregistry.servers}")
    private String servers;

    @Override
    public void start() {
        log.info(" ======> [ZwRegistryCenter] : start with server {}", servers);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void stop() {
        log.info(" ======> [ZwRegistryCenter] : stop with server {}", servers);
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instanceMeta) {
        log.info(" ======> [ZwRegistryCenter] : register instance {} for {}", instanceMeta, service);
        HttpInvoker.httpPost(JSON.toJSONString(instanceMeta), servers + "/reg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ======> [ZwRegistryCenter] : registered instance {} for {}", instanceMeta, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instanceMeta) {
        log.info(" ======> [ZwRegistryCenter] : unregister instance {} for {}", instanceMeta, service);
        HttpInvoker.httpPost(JSON.toJSONString(instanceMeta), servers + "/unreg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ======> [ZwRegistryCenter] : unregistered instance {} for {}", instanceMeta, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ======> [ZwRegistryCenter] : find all instance for {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/reg?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ======> [ZwRegistryCenter] : find all instances : {}", instances);
        return instances;
    }

    Map<String, Long> VERSIONS = new HashMap<>();

    ScheduledExecutorService executor;

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        executor.scheduleWithFixedDelay(() -> {
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            Long newVersion = HttpInvoker.httpGet(servers + "/version?service=" + service.toPath(), Long.class);
            log.info(" ======> [ZwRegistryCenter] : check version for {} : version = {} -> newVersion = {}", service, version, newVersion);
            if (newVersion > version) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(service.toPath(), newVersion);
            }
        }, 1000, 5000, TimeUnit.MILLISECONDS);
    }

}
