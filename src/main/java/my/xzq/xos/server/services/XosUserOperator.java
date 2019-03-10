package my.xzq.xos.server.services;


import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.model.User;

public interface XosUserOperator {

    Integer createUser(User user) throws XosException;

    User getUserInfo(String username) throws XosException;
}
