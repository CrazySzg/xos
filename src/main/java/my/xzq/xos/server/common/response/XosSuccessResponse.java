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

    private Integer successCode = XosConstant.SUCCESS;
    //返回json串
    private T successResult;

    private Date timestamp = new Date();

    private XosSuccessResponse(T successResult) {
        this.successResult = successResult;
    }

    public static <T> XosSuccessResponse<T> build(T successResult) {
        return new XosSuccessResponse<>(successResult);
    }

    public static <T> XosSuccessResponse<T> buildEmpty() {
        return new XosSuccessResponse<>(null);
    }
}
