package my.xzq.xos.server.configuration.security;

import my.xzq.xos.server.services.impl.XosUserService;
import my.xzq.xos.server.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.util.StringUtils;

/**
 * @author Administrator
 * @create 2019-03-07 11:28
 */
public class XosAuthenticationProvider implements AuthenticationProvider {


    private XosUserService userService;

    public XosAuthenticationProvider(XosUserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        XosAuthenticationToken authenticationToken = (XosAuthenticationToken)authentication;
        String principle = authenticationToken.getPrincipal();
        try {
            //获取该用户本次登录的signKey
            String userSignKey = JWTUtil.getUserSignKey(principle);
            //如果获取不到，说明是过期，或者该用户不存在
            if(StringUtils.isEmpty(userSignKey)) {
                throw new NonceExpiredException("Token expires");
            }
            String jws = authenticationToken.getJws();
            if(JWTUtil.validateToken(jws,userSignKey)) {
                XosAuthenticationToken authenticToken = new XosAuthenticationToken(principle);
                return authenticToken;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadCredentialsException("JWT  verify fail", e);
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(XosAuthenticationToken.class);
    }
}
