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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

    Map<String, Long> VERSIONS = new HashMap<>();

    ScheduledExecutorService consumerExecutor;

    ScheduledExecutorService providerExecutor;

    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();

    @Override
    public void start() {
        log.info(" ======> [ZwRegistryCenter] : start with server {}", servers);
        consumerExecutor = Executors.newSingleThreadScheduledExecutor();
        providerExecutor = Executors.newSingleThreadScheduledExecutor();
        providerExecutor.scheduleWithFixedDelay(() -> {
            RENEWS.keySet().forEach(instance -> {
                StringBuilder sb = new StringBuilder();
                for (ServiceMeta service : RENEWS.get(instance)) {
                    sb.append(service.toPath()).append(",");
                }
                String services = sb.toString();
                if (services.endsWith(",")) {
                    services = services.substring(0, services.length() - 1);
                }
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), servers + "/renews?services=" + services, Long.class);
                log.info(" ======> [ZwRegistryCenter] : renew instance {} for {} at {}", instance, services, timestamp);
            });
        },5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        log.info(" ======> [ZwRegistryCenter] : stop with server {}", servers);
        gracefulShutdown(consumerExecutor);
        gracefulShutdown(providerExecutor);
    }

    private void gracefulShutdown(ScheduledExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executorService.isShutdown()) {
                executorService.shutdownNow();
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
        RENEWS.add(instanceMeta, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instanceMeta) {
        log.info(" ======> [ZwRegistryCenter] : unregister instance {} for {}", instanceMeta, service);
        HttpInvoker.httpPost(JSON.toJSONString(instanceMeta), servers + "/unreg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ======> [ZwRegistryCenter] : unregistered instance {} for {}", instanceMeta, service);
        RENEWS.remove(instanceMeta, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ======> [ZwRegistryCenter] : find all instance for {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(servers + "/findAll?service=" + service.toPath(), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ======> [ZwRegistryCenter] : find all instances : {}", instances);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        consumerExecutor.scheduleWithFixedDelay(() -> {
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
