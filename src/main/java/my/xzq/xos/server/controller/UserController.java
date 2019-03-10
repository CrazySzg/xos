package my.xzq.xos.server.controller;

import my.xzq.xos.server.common.response.XosSuccessResponse;
import my.xzq.xos.server.dto.UserParam;
import my.xzq.xos.server.exception.XosException;
import my.xzq.xos.server.model.User;
import my.xzq.xos.server.services.impl.XosUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PostMapping("/register")
    public XosSuccessResponse<Integer> test(@Valid @RequestBody UserParam param) {
        try {
            User user = new User();
            BeanUtils.copyProperties(param,user);
            return XosSuccessResponse.build(userService.createUser(user));
        } catch (XosException e) {
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