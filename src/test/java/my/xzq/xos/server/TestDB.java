package my.xzq.xos.server;

import my.xzq.xos.server.mapper.UserMapper;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.utils.JsonUtil;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author Administrator
 * @create 2019-03-08 12:02
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDB {

    @Autowired
    private SqlSession sqlSession;

    @Autowired
    private Environment env;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test() {
        User xosUser = new User();
        xosUser.setUsername("x13zq");
        xosUser.setPassword("123");
        xosUser.setPhone("15626164246");
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        userMapper.insert(xosUser);
        System.out.println(xosUser);
    }

    @Test
    public void loadUser() throws IOException {
        User xosUser = userMapper.loadUserByUsername("xzq");
        System.out.println(xosUser);
        System.out.println(JsonUtil.toJson(xosUser));
    }



}
