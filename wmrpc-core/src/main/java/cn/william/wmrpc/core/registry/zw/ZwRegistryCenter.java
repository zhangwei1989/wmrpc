package cn.william.wmrpc.core.registry.zw;

import cn.william.wmrpc.core.api.RegistryCenter;
import cn.william.wmrpc.core.client.HttpInvoker;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ZwRegistryCenter
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/27
 */
@Slf4j
public class ZwRegistryCenter implements RegistryCenter {

    @Value("${zwregistry.server}")
    private String server;

    final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();

    MultiValueMap<InstanceMeta, String> RENEWS = new LinkedMultiValueMap<>();

    private ScheduledExecutorService consumerExecutorService;

    private ScheduledExecutorService providerExecutorService;

    @Override
    public void start() {
        log.info(" ======> [ZwRegistryCenter] -> ZwRegistryCenter started");
        // 启动注册中心时，初始化消费者和服务者各自的定时任务执行器
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        providerExecutorService = Executors.newSingleThreadScheduledExecutor();
        renews();
    }

    private void renews() {
        providerExecutorService.scheduleWithFixedDelay(() -> {
            RENEWS.keySet().forEach(instance -> {
                String services = String.join(",", RENEWS.get(instance));
                if (services.endsWith(",")) {
                    services.substring(0, services.length() - 1);
                }

                log.info(" ======> [ZwRegistryCenter] RENEWS : {}", RENEWS);
                log.info(" ======> [ZwRegistryCenter] services : {}", services);
                Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance), server + "/renews?services=" + services, Long.class);
                log.info(" ======> [ZwRegistryCenter] -> providerExecutorService renews completed, timestamp : {}", timestamp);
            });
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        gracefulShutdown(consumerExecutorService);
        gracefulShutdown(providerExecutorService);
        log.info(" ======> [ZwRegistryCenter] -> ZwRegistryCenter stopped");
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
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ======> [ZwRegistryCenter] -> register instance : {}, for service {}", instance, service);
        InstanceMeta instanceMeta = HttpInvoker.httpPost(JSON.toJSONString(instance), server + "/reg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ======> [ZwRegistryCenter] -> registered instance : {}, for service {}", instanceMeta, service);
        RENEWS.add(instance, service.toPath());
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ======> [ZwRegistryCenter] -> unregister instance : {}, for service {}", instance, service);
        InstanceMeta instanceMeta = HttpInvoker.httpPost(JSON.toJSONString(instance), server + "/unreg?service=" + service.toPath(), InstanceMeta.class);
        log.info(" ======> [ZwRegistryCenter] -> unregistered instance : {}, for service {}", instanceMeta, service);
        RENEWS.remove(instance, service.toPath());
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ======> [ZwRegistryCenter] -> find all instances for service start {}", service);
        List<InstanceMeta> instances = HttpInvoker.httpGet(server + "/findAll?service=" + service.toPath(), new TypeReference<>() {
        });
        log.info(" ======> [ZwRegistryCenter] -> find all instances : {}, for service {} finished", instances, service);
        return instances;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        String servicePath = service.toPath();

        // 定时循环请求VERSIONS中，获取注册中心的该 service 版本
        consumerExecutorService.scheduleWithFixedDelay(() -> {
            log.info(" ======> [ZwRegistryCenter] -> get version of service : {}", service);
            Long version = VERSIONS.getOrDefault(servicePath, -1L);
            Long rcVersion = HttpInvoker.httpGet(server + "/version?service=" + servicePath, Long.class);
            log.info(" ======> [ZwRegistryCenter] -> check version for service : {}, version = {} -> rcVersion = {}", service, version, rcVersion);
            // 与本地版本做比对，版本落后时，重新 fetchAll
            if (version < rcVersion) {
                List<InstanceMeta> instances = fetchAll(service);
                listener.fire(new Event(instances));
                VERSIONS.put(servicePath, rcVersion);
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

}
