package cn.william.wmrpc.core.cluster;

import cn.william.wmrpc.core.api.Router;
import cn.william.wmrpc.core.meta.InstanceMeta;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 灰度路由
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/4/3
 */
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {

    @Setter
    private int grayRatio;

    private final Random random = new Random();

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }

    @Override
    public List<InstanceMeta> route(List<InstanceMeta> providers) {
        if (providers == null || providers.size() <= 1) {
            return providers;
        }

        // 分类
        List<InstanceMeta> normalNodes = new ArrayList<>();
        List<InstanceMeta> grayNodes = new ArrayList<>();

        providers.forEach(p -> {
            if ("true".equals(p.getParameters().get("gray"))) {
                grayNodes.add(p);
            } else {
                normalNodes.add(p);
            }
        });

        log.debug("grayRouter grayNodes/normalNodes, grayRatio ======> {}, {}, {}",
                grayNodes.size(), normalNodes.size(), grayRatio);

        if (normalNodes.isEmpty() || grayNodes.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            return normalNodes;
        } else if (grayRatio >= 100) {
            return grayNodes;
        }

        if (random.nextInt(100) < grayRatio) {
            log.debug("grayRouter grayNodes ===> {}", grayNodes);
            return grayNodes;
        } else {
            log.debug("grayRouter normalNodes ===> {}", normalNodes);
            return normalNodes;
        }
    }
}
