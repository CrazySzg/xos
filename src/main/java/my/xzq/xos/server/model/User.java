package my.xzq.xos.server.model;

import lombok.Data;

import java.util.Date;

/**
 * @author Administrator
 * @create 2019-03-07 19:53
 */
@Data
public class User {

    private String id;
    private String username;
    private String password;
    private String avator;
    private String email;
    private String phone;
    private Long used;
    private Long capacity = 1024*1024*1024*5L;
    private Date createTime;
    private Date updateTime;

}
