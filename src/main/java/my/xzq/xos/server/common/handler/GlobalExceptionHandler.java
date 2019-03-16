package my.xzq.xos.server.common.handler;

import my.xzq.xos.server.common.response.XosFailureResponse;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.utils.JsonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * @author Administrator
 * @create 2019-03-09 11:39
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(XosException.class)
    @ResponseStatus(HttpStatus.OK)
    public String handleXosException(HttpServletRequest request, XosException exception) throws IOException {
        String url = this.getPath(request);
        XosFailureResponse response =
                new XosFailureResponse(exception.getCode(), exception.getMessage(), new Date(), url);
        return JsonUtil.toJson(response,true);
    }

   /* @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception ex) throws IOException {
        String url = this.getPath(request);
        XosFailureResponse response =
                new XosFailureResponse(XosConstant.UNEXPECTED_ERROR, ex.getMessage(), new Date(), url);
        return JsonUtil.toJson(response,true);
    }*/


    public String getPath(HttpServletRequest request) {
        String url = request.getServletPath();

        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
        }
        return url;
    }
}

