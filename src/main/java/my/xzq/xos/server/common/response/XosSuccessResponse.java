package my.xzq.xos.server.common.response;

import lombok.Data;
import my.xzq.xos.server.common.XosConstant;

import java.util.Date;

/**
 * @author Administrator
 * @create 2019-03-09 11:51
 */
@Data
public class XosSuccessResponse<T> implements XosResponse {

    private Integer code = XosConstant.SUCCESS;
    //返回json串
    private T data;

    private Date timestamp = new Date();

    private XosSuccessResponse(T data) {
        this.data = data;
    }

    public static <T> XosSuccessResponse<T> build(T data) {
        return new XosSuccessResponse<>(data);
    }

    public static <T> XosSuccessResponse<T> buildEmpty() {
        return new XosSuccessResponse<>(null);
    }
}
