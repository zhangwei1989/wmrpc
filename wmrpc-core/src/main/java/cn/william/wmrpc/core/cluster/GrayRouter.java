package cn.william.wmrpc.core.cluster;

import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由器
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/4
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    @Setter
    private int grayRatio;

    private Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        if (grayRatio <= 0 || grayRatio > 100) {
            return providers;
        }

        // 分组
        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        if (grayNodes.isEmpty() || normalNodes.isEmpty()) {
            return providers;
        }

        // 使用随机的方法确定是否访问灰度节点
        if (random.nextInt(100) < grayRatio) {
            return grayNodes;
        } else {
            return normalNodes;
        }
    }
}
