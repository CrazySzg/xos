package my.xzq.xos.server.mapper;

import my.xzq.xos.server.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    Integer insert(User xosUser);

    User loadUserByUsername(@Param("username") String username);

    User loadUserByEmail(@Param("email") String email);

    User getUserInfoWithoutPassword(@Param("username") String username);

    Integer updateUserCapacity(@Param("used") Long used,@Param("userUUID") String userUUID);
}
