package my.xzq.xos.server.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

/**
 * @author Administrator
 * @create 2019-03-09 10:56
 */
@Data
public class UserParam {
    @NotEmpty(message = "用户名不能为空")
    @Length(min = 1,max = 50,message = "用户名长度不正确")
    private String username;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 50,message = "密码长度不正确")
    private String password;
    @NotEmpty(message = "二次确认密码不能为空")
    @Length(min = 6,max = 50,message = "二次确认密码长度不正确")
    private String confirmPwd;
    @Email(regexp = "^([A-Za-z0-9_\\-\\.])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,4})$")
    private String email;
    /*@Pattern(regexp = "\\d{11}")
    private String phone;*/
}
