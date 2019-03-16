package my.xzq.xos.server.services;


import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.model.User;

public interface XosUserOperator {

    String createUser(User user) throws XosException;

    User getUserInfo(String username) throws XosException;

    Integer updateUserCapacity(String userUUID, long used) throws XosException;
}
