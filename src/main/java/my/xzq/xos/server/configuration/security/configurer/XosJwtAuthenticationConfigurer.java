package my.xzq.xos.server.configuration.security.configurer;

import my.xzq.xos.server.configuration.security.filter.XosJwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

/**
 * 鉴权过滤器配置
 *
 * @author Administrator
 * @create 2019-03-06 22:48
 */
public class XosJwtAuthenticationConfigurer<T extends XosJwtAuthenticationConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T,B> {

    private XosJwtAuthenticationFilter filter;

    public XosJwtAuthenticationConfigurer() {
        this.filter = new XosJwtAuthenticationFilter();
    }

    @Override
    public void configure(B builder) throws Exception {
        filter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
        builder.addFilterBefore(postProcess(filter),LogoutFilter.class);
    }

    public XosJwtAuthenticationConfigurer<T, B> setAuthenticationSuccessHandler(AuthenticationSuccessHandler handler) {
        filter.setAuthenticationSuccessHandler(handler);
        return this;
    }

    public XosJwtAuthenticationConfigurer<T, B> setUndesiredAuthenticationUrls(String ... urls) {
        filter.setUndesiredRequestMatcher(urls);
        return this;
    }


    public XosJwtAuthenticationConfigurer<T, B> permissiveRequestUrls(String ... urls){
        filter.setPermissiveUrl(urls);
        return this;
    }

    public XosJwtAuthenticationConfigurer<T, B> setAuthenticationFailureHandler(AuthenticationFailureHandler handler) {
        filter.setAuthenticationFailureHandler(handler);
        return this;
    }
}
