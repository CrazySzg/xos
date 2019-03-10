package my.xzq.xos.server.configuration.security.filter;

import my.xzq.xos.server.configuration.security.XosAuthenticationToken;
import my.xzq.xos.server.utils.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 校验token有效性
 *
 * @author Administrator
 * @create 2019-03-06 20:50
 */
public class XosJwtAuthenticationFilter extends OncePerRequestFilter {

    private List<RequestMatcher> undesiredAuthenticationRequestMatchers;
    private List<RequestMatcher> permissiveRequestMatchers;
    private AuthenticationManager authenticationManager;
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;
    private static final String AUTHORIZATION_HEADER = "Authorization";

    public XosJwtAuthenticationFilter() {
       this.undesiredAuthenticationRequestMatchers = new ArrayList<>();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (undesiredAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authResult = null;
        AuthenticationException failed = null;
        try {
            String jws = this.getJWTToken(request);
            if(StringUtils.hasText(jws)) {
                String principle = JWTUtil.getJwsOwner(jws);
                XosAuthenticationToken token = new XosAuthenticationToken(principle,jws);
                authResult = this.getAuthenticationManager().authenticate(token);
            } else {
                failed = new InsufficientAuthenticationException("JWT validate fail");
            }
        } catch (AuthenticationException e) {
            e.printStackTrace();
            failed = e;
        }

        if(authResult != null) {
            successfulAuthentication(request, response, filterChain, authResult);
        } else if(!permissiveRequest(request)){
            unsuccessfulAuthentication(request, response, failed);
            return;
        }
        filterChain.doFilter(request,response);
    }

    private String getJWTToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        return header;
    }


    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain, Authentication authResult)
            throws IOException, ServletException{
        SecurityContextHolder.getContext().setAuthentication(authResult);
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    protected boolean permissiveRequest(HttpServletRequest request) {
        if(permissiveRequestMatchers == null)
            return false;
        for(RequestMatcher permissiveMatcher : permissiveRequestMatchers) {
            if(permissiveMatcher.matches(request))
                return true;
        }
        return false;
    }

    public void setPermissiveUrl(String... urls) {
        if(permissiveRequestMatchers == null)
            permissiveRequestMatchers = new ArrayList<>();
        for(String url : urls)
            permissiveRequestMatchers.add(new AntPathRequestMatcher(url));
    }

    public void setUndesiredRequestMatcher(String... urls) {
        for(String url : urls) {
            undesiredAuthenticationRequestMatchers.add(new AntPathRequestMatcher(url));
        }
    }


    private boolean undesiredAuthentication(HttpServletRequest httpServletRequest) {
        for(RequestMatcher requestMatcher : undesiredAuthenticationRequestMatchers) {
            if(requestMatcher.matches(httpServletRequest)) {
                return true;
            }
        }
        return false;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler handler) {
        this.successHandler = handler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler handler) {
        this.failureHandler = handler;
    }
}
