package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Component
@WmProvider
public class UserServiceImpl implements UserService {

    @Autowired
    Environment environment;

    String timeoutPorts = "8080";

    @Override
    public User findById(int id) {
        return new User(id, "WM-" + "V10-"
                + environment.getProperty("server.port")
                + "_"
                + System.currentTimeMillis());
    }

    @Override
    public User findById(int id, String name) {
        return new User(id, "WM-" + name + "_" + System.currentTimeMillis());
    }

    @Override
    public long getId(long id) {
        return id;
    }

    @Override
    public long getId(User user) {
        return user.getId().longValue();
    }

    @Override
    public long getId(float id) {
        return 1L;
    }

    @Override
    public int[] getIds() {
        return new int[] {100,200,300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1,2,3};
    }

    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }

    @Override
    public String getName() {
        return "Wmrpc";
    }

    @Override
    public String getName(int id) {
        return "Cola-" + id;
    }

    public User find(int timeout) {
        String port = environment.getProperty("server.port");
        if (Arrays.stream(timeoutPorts.split(",")).anyMatch(port::equals)) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return new User(101, "Wm101-" + port);
    }

    public void setTimeoutPorts(String timeoutPorts) {
        this.timeoutPorts = timeoutPorts;
    }

}
