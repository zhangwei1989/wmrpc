package cn.william.wmrpc.core.registry.zw;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * health check for zw registry center
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/29
 */
@Slf4j
public class ZwHealthChecker {

    private ScheduledExecutorService consumerExecutorService;

    private ScheduledExecutorService providerExecutorService;

    public void start() {
        log.info(" ======> [ZwHealthChecker] -> ZwHealthChecker started");
        // 启动注册中心时，初始化消费者和服务者各自的定时任务执行器
        consumerExecutorService = Executors.newSingleThreadScheduledExecutor();
        providerExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void stop() {
        gracefulShutdown(consumerExecutorService);
        gracefulShutdown(providerExecutorService);
        log.info(" ======> [ZwHealthChecker] -> ZwHealthChecker stopped");
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

    public void consumerCheck(Callback callback) {
        consumerExecutorService.scheduleWithFixedDelay(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(" ======> [ZwHealthChecker] -> consumerCheck encounter a error : {}", e.getMessage());
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public void providerCheck(Callback callback) {
        providerExecutorService.scheduleWithFixedDelay(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(" ======> [ZwHealthChecker] -> providerCheck encounter a error : {}", e.getMessage());
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    public interface Callback {
        void call() throws Exception;
    }

}
