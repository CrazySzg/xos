package my.xzq.xos.server.services.impl;

import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.common.XosErrMsgProperties;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.mapper.UserMapper;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.services.XosUserOperator;
import my.xzq.xos.server.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Administrator
 * @create 2019-03-06 20:44
 */
@Service
public class XosUserService implements UserDetailsService, XosUserOperator {

    @Autowired
    private UserMapper userMapper;

    private PasswordEncoder passwordEncoder;

    @Autowired
    private XosErrMsgProperties xosErrProperties;

    public XosUserService() {
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();  //默认使用 bcrypt， strength=10
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.loadUserByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("username " + username + " not found");
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER").build();

    }

    @Override
    public User getUserInfo(String username) throws XosException {
        return userMapper.getUserInfoWithoutPassword(username);
    }

    @Override
    @Transactional
    public String createUser(User user) throws XosException {
        Date now = new Date();
        User loadedUser = userMapper.loadUserByUsername(user.getUsername());
        if (loadedUser != null) {
            throw new XosException(XosConstant.REGISTER_USER_FAIL);
        }
        String userUUID = UUIDUtil.getUUIDString();
        user.setUserUUID(userUUID);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUsed(0L);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return userUUID;
    }

    @Override
    public Integer updateUserCapacity(String userUUID, long used) throws XosException {
        return userMapper.updateUserCapacity(used,userUUID);
    }
}
