package my.xzq.xos.server.exception;

import lombok.Data;
import my.xzq.xos.server.common.XosConstant;

@Data
public class XosValidationException extends XosException {


    private Integer code = XosConstant.BAD_PARAM;
    private String message;

    public XosValidationException(String message) {
        this.message = message;
    }
}
