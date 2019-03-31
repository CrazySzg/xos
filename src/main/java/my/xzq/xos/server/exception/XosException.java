package my.xzq.xos.server.exception;

import my.xzq.xos.server.common.XosErrMsgProperties;
import my.xzq.xos.server.utils.SpringContextUtil;

/**
 * @author Administrator
 * @create 2019-03-09 11:41
 */
public class XosException extends RuntimeException {

    private Integer code;
    private String message;

    public XosException() {}

    public XosException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public XosException(Integer code) {
        this.code = code;
        XosErrMsgProperties msgProperties = SpringContextUtil.getBean(XosErrMsgProperties.class);
        this.message = msgProperties.getMessage(code);
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
