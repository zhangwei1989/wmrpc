package cn.william.wmrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 服务元信息
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/22
 */
@Data
@Builder
public class ServiceMeta {

    private String app;

    private String namespace;

    private String name;

    private String env;

    private String version;

    private Map<String, String> parameters;

    public String toPath() {
        return String.format("%s_%s_%s_%s_%s", app, namespace, name, env, version);
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
