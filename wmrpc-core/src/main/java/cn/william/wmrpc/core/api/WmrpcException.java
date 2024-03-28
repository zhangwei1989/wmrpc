package cn.william.wmrpc.core.api;

import lombok.Data;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/27
 */
@Data
public class WmrpcException extends RuntimeException {

    private String errcode;

    public WmrpcException() {
    }

    public WmrpcException(String message) {
        super(message);
    }

    public WmrpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public WmrpcException(Throwable cause) {
        super(cause);
    }

    public WmrpcException(Throwable cause, String errcode) {
        super(cause);
        this.errcode = errcode;
    }

    // X -> 技术类异常
    // Y -> 业务类异常
    // Z -> unknown,搞不清楚，搞清楚后再归类到 X 或 Y
    public static final String SocketTimeOutEx = "X001" + "-" + "http_invoke_timeout";
    public static final String NoSuchMethodEx = "Y001" + "-" + "method_not_exists";
    public static final String UnknownEx = "Z001" + "-" + "unknown";
}
