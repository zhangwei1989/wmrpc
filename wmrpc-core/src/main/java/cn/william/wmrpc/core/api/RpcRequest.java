package cn.william.wmrpc.core.api;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/7
 */
@Data
public class RpcRequest {

    private String service;  // 接口：cn.william.wmrpc.demo.api.UserService

    private String methodSign;   // 方法：findById

    private Object[] args;   // 参数：100

    private Map<String, String> parameters = new HashMap<>(); // 向服务端携带额外参数媒介

}
