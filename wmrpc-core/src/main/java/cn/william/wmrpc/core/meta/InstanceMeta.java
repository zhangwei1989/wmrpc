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
public class InstanceMeta {

    private String scheme;

    private String host;

    private Integer port;

    private String context;

    public String toPath() {
        return String.format("%s_%d_%s", host, port, context);
    }

    public String http() {
        return String.format("%s://%s:%d", scheme, host, port);
    }
}
