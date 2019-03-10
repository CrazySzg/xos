package my.xzq.xos.server.configuration.security.handler;

import my.xzq.xos.server.utils.JWTUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Administrator
 * @create 2019-03-07 14:59
 */
@Component
public class XosTokenClearLogoutHandler implements LogoutHandler {


    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        clearToken(authentication);
     }


    private void clearToken(Authentication authentication) {
        if (authentication != null) {
            String principal = (String) authentication.getPrincipal();
            if(StringUtils.hasText(principal)) {
                JWTUtil.clearUserSignKey(principal);
            }
        }
    }
}
