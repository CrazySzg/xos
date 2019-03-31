package my.xzq.xos.server.controller;

import my.xzq.xos.server.common.XosConstant;
import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.request.UserParam;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.exception.XosValidationException;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.services.XosService;
import my.xzq.xos.server.services.impl.XosUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

/**
 * @author Administrator
 * @create 2019-03-07 15:08
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private XosUserService userService;

    @Autowired
    private XosService xosService;

    @PostMapping("/register")
    public XosSuccessResponse<Integer> register(@Valid @RequestBody UserParam param, BindingResult bindingResult) throws Exception {
        if(bindingResult.hasErrors()) {
            ObjectError error = bindingResult.getAllErrors().get(0);
            throw new XosValidationException(error.getDefaultMessage());
        }
        try {
            String password = param.getPassword();
            String confirmPwd = param.getConfirmPwd();
            if(!StringUtils.equals(password,confirmPwd)) {
                throw new XosException(XosConstant.PASSWORD_NOT_EQUAL);
            }
            User xosUser = new User();
            BeanUtils.copyProperties(param, xosUser);
            String userUUID = userService.createUser(xosUser);
            xosService.createBucketStore(userUUID);
            return XosSuccessResponse.buildEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/getUserInfo")
    public XosSuccessResponse<User> getUserInfo(@NotEmpty @RequestParam("username") String username) {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            if(authentication.getPrincipal().equals(username))
                return XosSuccessResponse.build(userService.getUserInfo(username));
            else return XosSuccessResponse.buildEmpty();
        } catch (XosException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
