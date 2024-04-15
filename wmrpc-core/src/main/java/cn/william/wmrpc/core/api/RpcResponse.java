package cn.william.wmrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RpcResponse 类
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {

    boolean status;       // 状态：true

    Object data;          // new User

    RpcException exception;

}
