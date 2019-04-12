package my.xzq.xos.server.configuration.security;

import my.xzq.xos.server.configuration.security.configurer.XosJwtAuthenticationConfigurer;
import my.xzq.xos.server.configuration.security.configurer.XosLoginConfigurer;
import my.xzq.xos.server.configuration.security.filter.OptionsRequestFilter;
import my.xzq.xos.server.configuration.security.handler.*;
import my.xzq.xos.server.services.impl.XosUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * @author Administrator
 * @create 2019-03-05 20:48
 *  https://www.baeldung.com/securing-a-restful-web-service-with-spring-security
 */
@EnableWebSecurity
@Configuration
public class XosWebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private RestAuthenticationEntryPoint entryPoint;

    @Autowired
    private XosAuthenticationSuccessHandler successHandler;

    @Autowired
    private XosAnthenticationFailureHandler failureHandler;

    @Autowired
    private XosJwtRefreshSuccessHandler refreshSuccessHandler;

    @Autowired
    private XosTokenClearLogoutHandler logoutHandler;

    @Autowired
    private XosUserService xosUserService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable() //没有session，所以可以禁用
            .sessionManagement().disable()  //因为前后端分离，所以不需要session
            .cors()
            .and()
                .headers()
                .frameOptions().sameOrigin()
            .and()
            .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class)
            .authorizeRequests()
                .antMatchers("/image/**").permitAll()
                .antMatchers("/druid/**").permitAll()
                .antMatchers("/main/download/**").permitAll()
                .antMatchers("/main/preview/**").permitAll()
                .mvcMatchers("/user/register").permitAll()
                .anyRequest().authenticated()
            .and()
            .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                new Header("Access-control-Allow-Origin","*"),
                new Header("Access-Control-Expose-Headers","Authorization"))))
            .and()
            .formLogin().disable()
            .apply(new XosLoginConfigurer<>())
                .loginSuccessHandler(successHandler).loginFailureHandler(failureHandler)
            .and()
            .apply(new XosJwtAuthenticationConfigurer<>())
                .setAuthenticationFailureHandler(failureHandler)
                .setAuthenticationSuccessHandler(refreshSuccessHandler)
                .permissiveRequestUrls("/logout","/main/download/**","/main/preview/**")
                .setUndesiredAuthenticationUrls("/login","/druid/**","/user/register")
            .and()
                .logout()
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
            .and()
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(xosAuthenticationProvider())
            .authenticationProvider(daoAuthenticationProvider());
    }


    //这个是用来检查请求头中的token的
    @Bean
    public AuthenticationProvider xosAuthenticationProvider() {
        return new XosAuthenticationProvider(xosUserService);
    }

    //这个是在登录校验用户名和密码的
    @Bean("daoAuthenticationProvider")
    protected AuthenticationProvider daoAuthenticationProvider() throws Exception{
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider(); //其中的support方法支持usernamepasswordAuthenticationToken
        daoProvider.setUserDetailsService(xosUserService);
        return daoProvider;
    }


    @Override
    public UserDetailsService userDetailsService() {
        return xosUserService;
    }


    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","HEAD", "OPTION","PUT","DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
