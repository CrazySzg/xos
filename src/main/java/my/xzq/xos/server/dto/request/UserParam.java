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
    @NotEmpty
    @Length(min = 1,max = 50)
    private String username;
    @NotEmpty
    @Length(min = 8,max = 50)
    private String password;
  /*  @Email(regexp = "^([A-Za-z0-9_\\-\\.])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,4})$")
    private String email;
    @Pattern(regexp = "\\d{11}")
    private String phone;*/
}
