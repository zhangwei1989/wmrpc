package cn.william.wmrpc.demo.provider;

import cn.william.wmrpc.core.annotation.WmProvider;
import cn.william.wmrpc.core.api.RpcContext;
import cn.william.wmrpc.demo.api.User;
import cn.william.wmrpc.demo.api.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@WmProvider
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private Environment environment;

    private String ports = "8081,8094";

    @Override
    public User findById(int id) {
        return new User(id, "WM-" + environment.getProperty("server.port")
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
        return new int[]{100, 200, 300};
    }

    @Override
    public long[] getLongIds() {
        return new long[]{1, 2, 3};
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

    @Override
    public Map<String, List<User>> getMutipleUser(Map<String, List<User>> map) {
        User user = map.get("userMap").get(0);
        return map;
    }

    @Override
    public User find(int timeout) {
        String port = environment.getProperty("server.port");
        if (Arrays.stream(ports.split(",")).anyMatch(port::equals)) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return new User(101, "Wmrpc-" + port);
    }

    @Override
    public void setPorts(String ports) {
        this.ports = ports;
        log.debug("current ports set to : {}", ports);
    }

    @Override
    public List<User> getList(List<User> userList) {
        User[] users = userList.toArray(new User[userList.size()]);
        System.out.println(" ==> userList.toArray()[] = ");
        Arrays.stream(users).forEach(System.out::println);
        userList.add(new User(2024, "KK2024"));
        return userList;
    }

    @Override
    public Map<String, User> getMap(Map<String, User> userMap) {
        userMap.values().forEach(x -> System.out.println(x.getClass()));
        User[] users = userMap.values().toArray(new User[userMap.size()]);
        System.out.println(" ==> userMap.values().toArray()[] = ");
        Arrays.stream(users).forEach(System.out::println);
        userMap.put("A2024", new User(2024, "KK2024"));
        return userMap;
    }

    @Override
    public User[] findUsers(User[] users) {
        return users;
    }

    @Override
    public Boolean getFlag(boolean flag) {
        return !flag;
    }

    @Override
    public User findById(long id) {
        return new User(Long.valueOf(id).intValue(), "KK");
    }

    @Override
    public User ex(boolean flag) {
        if (flag) throw new RuntimeException("just throw an exception");
        return new User(100, "KK100");
    }

    @Override
    public String echoParameter(String key) {
        System.out.println(" ======>>> RpcContext.ContextParameters: ");

        RpcContext.getContextParams().forEach((k, v) -> System.out.println(k + " -> " + v));
        return RpcContext.getContextParam(key);
    }
}