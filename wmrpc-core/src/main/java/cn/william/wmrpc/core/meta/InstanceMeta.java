package cn.william.wmrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 服务实例元信息
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/22
 */
@Data
@Builder
@AllArgsConstructor
public class InstanceMeta {

    private String schema;

    private String host;

    private Integer port;

    private String context;

    private Map<String, String> parameters;

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String http() {
        return String.format("%s://%s:%d", schema, host, port) + "/wmrpc";
    }

    public String toMetas() {
        return JSON.toJSONString(parameters);
    }
}
