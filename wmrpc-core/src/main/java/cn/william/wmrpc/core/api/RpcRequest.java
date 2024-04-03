package cn.william.wmrpc.core.api;

import lombok.Data;

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

}
