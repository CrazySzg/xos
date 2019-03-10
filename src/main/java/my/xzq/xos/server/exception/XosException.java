package my.xzq.xos.server.exception;

/**
 * @author Administrator
 * @create 2019-03-09 11:41
 */
public class XosException extends RuntimeException {

    private Integer code;
    private String message;

    public XosException(Integer code, String message) {
        this.code = code;
        this.message = message;
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
