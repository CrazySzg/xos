package my.xzq.xos.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * @author Administrator
 * @create 2019-03-09 11:51
 */
@Data
@AllArgsConstructor
public class XosFailureResponse implements XosResponse {

    private Integer errorCode;
    private String errorMessage;
    private Date timestamp;
    private String path;
}
