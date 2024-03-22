package cn.william.wmrpc.core.meta;

import lombok.Builder;
import lombok.Data;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
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

    public String toPath() {
        return String.format("%s_%s_%s_%s_%s", app, namespace, name, env, version);
    }
}
