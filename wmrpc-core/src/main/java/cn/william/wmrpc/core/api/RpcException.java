package cn.william.wmrpc.core.api;

import lombok.Data;

/**
 * Description for this class.
 *
 * @Author : zhangwei(331874675@qq.com)
 * @Create : 2024/3/28
 */
@Data
public class RpcException extends RuntimeException{

    private String errCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(String message, String errCode) {
        super(message);
        this.errCode = errCode;
    }

    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.errCode = errCode;
    }

    public static final String SOCKET_TIMEOUT_ERRCODE = "X" + "001" + "-" + "socket_timeout";

    public static final String USER_NOT_FOUND_ERRCODE = "Y" + "001" + "-" + "user_not_found";

    public static final String TPSLIMIT_EXCEED_ERRCODE = "Y" + "002" + "-" + "tpsLimit_exceed";

    public static final String UNKNOWN_ERRCODE = "Z" + "001" + "-" + "unknown";

}
