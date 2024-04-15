package cn.william.wmrpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Apollo配置中心配置变更监听器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/15
 */
@Data
@Slf4j
public class ApolloChangedEventListener implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @ApolloConfigChangeListener("${apollo.bootstrap.namespaces}")
    void changeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            log.info(" ======> Found change - {}", changeEvent.getChange(key).toString());
        }

        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }

}
