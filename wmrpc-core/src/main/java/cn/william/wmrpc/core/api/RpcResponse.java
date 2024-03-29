package cn.william.wmrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/7
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcResponse {

    boolean status;  // 状态：true

    Object data;          // new User

    Exception exception;

}
