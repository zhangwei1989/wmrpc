package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
@WmProvider
public class UserServiceImpl implements UserService {

    @Override
    public User findById(int id) {
        return new User(id, "WM-" + System.currentTimeMillis());
    }

}
