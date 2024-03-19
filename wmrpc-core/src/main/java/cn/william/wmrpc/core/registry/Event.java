package cn.william.wmrpc.core.registry;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Description for this class.
 *
 * @Author : zhangwei(zhangwei19890518@gmail.com)
 * @Create : 2024/3/19
 */
@Data
@AllArgsConstructor
public class Event {

    List<String> data;
}
