package cn.william.wmrpc.demo.api;

public interface UserService {

    User findById(int id);

    int getId(int id);

    String getName();
}
