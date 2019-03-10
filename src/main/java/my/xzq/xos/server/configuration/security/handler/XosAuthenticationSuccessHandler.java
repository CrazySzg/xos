package my.xzq.xos.server.configuration.security.handler;

import my.xzq.xos.server.utils.JWTUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @create 2019-03-05 23:30
 */
@Component
public class XosAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        UserDetails principle = (UserDetails) authentication.getPrincipal();
        String token = JWTUtil.generateToken(principle);
        response.setHeader("Authorization",token);
    }

}