package my.xzq.xos.server.configuration.security.configurer;

import my.xzq.xos.server.configuration.security.filter.XosUsernamePasswordAuthenticationFilter;
import my.xzq.xos.server.configuration.security.handler.XosAnthenticationFailureHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

/**
 * 注册XosUsernamePasswordAuthenticationFilter
 *
 * @author Administrator
 * @create 2019-03-06 21:12
 */
public class XosLoginConfigurer<T extends XosLoginConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T,B> {

    private XosUsernamePasswordAuthenticationFilter authenticationFilter;

    public XosLoginConfigurer() {
        this.authenticationFilter = new XosUsernamePasswordAuthenticationFilter();
    }

    @Override
    public void configure(B builder) throws Exception {
        authenticationFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        authenticationFilter.setAuthenticationFailureHandler(new XosAnthenticationFailureHandler());
        authenticationFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        XosUsernamePasswordAuthenticationFilter postProcess = postProcess(authenticationFilter);
        //logoutfilter 判断请求路径是不是退出，如果是直接return，假设能够走到logoutfilter之后说明不是请求登出
        builder.addFilterAfter(postProcess,LogoutFilter.class);
    }

    public XosLoginConfigurer<T,B> loginSuccessHandler(AuthenticationSuccessHandler handler) {
        this.authenticationFilter.setAuthenticationSuccessHandler(handler);
        return this;
    }

    public XosLoginConfigurer<T,B> loginFailureHandler(AuthenticationFailureHandler handler) {
        this.authenticationFilter.setAuthenticationFailureHandler(handler);
        return this;
    }
}
