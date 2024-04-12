package cn.william.wmrpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Apollo 配置变更监听器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/12
 */
@Data
@Slf4j
public class ApolloChangedListener implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @ApolloConfigChangeListener({"app2", "application"})
    private void changeHandler(ConfigChangeEvent changeEvent) {
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - {}", change.toString());
        }

        // 更新相应的 bean 的属性值，主要是存在@ConfigurationProperties 注解的 bean
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
