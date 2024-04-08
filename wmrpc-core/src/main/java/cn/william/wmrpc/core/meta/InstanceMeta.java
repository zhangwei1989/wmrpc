package cn.william.wmrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务实例的元数据
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstanceMeta {

    private String schema;

    private String host;

    private String port;

    private String context;

    private boolean status; // online or offline

    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String schema, String host, String port, String context) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%s", host, port);
    }

    public static InstanceMeta http(String host, String port) {
        return new InstanceMeta("http", host, port, "wmrpc");
    }

    public String toUrl() {
        return String.format("%s://%s:%s/%s", schema, host, port, context);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }
}
