package cn.william.wmrpc.core.registry;

import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/20
 */
@Data
@AllArgsConstructor
public class Event {

    List<InstanceMeta> data;
}
