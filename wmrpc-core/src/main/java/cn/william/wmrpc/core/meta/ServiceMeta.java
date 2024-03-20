package cn.william.wmrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述服务元数据
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMeta {

    private String app;

    private String namespace;

    private String env;

    private String name;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
