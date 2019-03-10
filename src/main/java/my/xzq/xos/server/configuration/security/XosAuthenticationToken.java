package my.xzq.xos.server.configuration.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Administrator
 * @create 2019-03-07 11:29
 */
public class XosAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    private final String principal;
    private String credentials;
    private String jws;

    public XosAuthenticationToken(String principal,String jws) {
        //此处默认只有USER角色
        super(Collections.emptyList());
        this.principal = principal;
        this.jws = jws;
    }

    public XosAuthenticationToken(String principal) {
        //此处默认只有USER角色
        super(Arrays.asList(new SimpleGrantedAuthority("USER")));
        this.principal = principal;
    }

    @Override
    public void setDetails(Object details) {
        super.setDetails(details);
        this.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return credentials;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    public String getJws() {
        return jws;
    }
}
