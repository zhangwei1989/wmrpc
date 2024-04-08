package cn.william.wmrpc.demo.api;

import java.util.List;
import java.util.Map;

public interface UserService {

    User findById(int id);

    User findById(int id, String name);

    long getId(long id);

    long getId(User user);

    long getId(float id);

    int[] getIds();

    long[] getLongIds();

    int[] getIds(int[] ids);

    String getName();

    String getName(int id);

    User find(int timeout);

    public void setTimeoutPorts(String timeoutPorts);

    public List<User> getList(List<User> userList);

    public Map<String, User> getMap(Map<String, User> userMap);

    Boolean getFlag(boolean flag);

    User[] findUsers(User[] users);

    User findById(long id);

    User ex(boolean flag);

    String echoParameter(String key);

}
