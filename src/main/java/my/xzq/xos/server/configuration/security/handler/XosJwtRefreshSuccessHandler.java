package my.xzq.xos.server.configuration.security.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @create 2019-03-06 22:43
 */
@Component
public class XosJwtRefreshSuccessHandler implements AuthenticationSuccessHandler {


    //guava 中的 expireAfterAccess 起到相似的作用，因此此处不需要做额外的实现
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

    }
}
