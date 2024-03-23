package cn.william.wmrpc.core.api;

import lombok.Data;
import lombok.ToString;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7
 */
@Data
@ToString
public class RpcRequest {

    private String service;  // 接口：cn.william.wmrpc.demo.api.UserService

    private String methodSign;   // 方法：findById

    private Object[] args;   // 参数：100

}
