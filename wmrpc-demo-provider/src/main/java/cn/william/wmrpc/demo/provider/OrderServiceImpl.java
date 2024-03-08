package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.demo.api.Order;
import cn.william.wmrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@WmProvider
public class OrderServiceImpl implements OrderService {

    @Override
    public Order findById(Integer id) {
        return new Order(id, 15.6F);
    }

}
