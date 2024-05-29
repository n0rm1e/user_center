package com.normie.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.normie.user_center.common.BaseResponse;
import com.normie.user_center.common.ErrorCode;
import com.normie.user_center.common.ResultUtils;
import com.normie.user_center.exception.BusinessException;
import com.normie.user_center.model.User;
import com.normie.user_center.model.request.UserLoginRequest;
import com.normie.user_center.model.request.UserRegisterRequest;
import com.normie.user_center.service.ChatServer;
import com.normie.user_center.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.normie.user_center.constant.UserConstant.ADMIN_ROLE;
import static com.normie.user_center.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = { "*" } )
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private ChatServer chatServer;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("尝试登录");
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        Gson gson = new Gson();
        User user = userService.userLogin(userAccount, userPassword, request);
        String userJson = gson.toJson(user);
        System.out.println("userJson: " + userJson);
        String encodedUserJson = Base64.getEncoder().encodeToString(userJson.getBytes());
        Cookie loginToken = new Cookie("loginToken", encodedUserJson);
        loginToken.setValue(loginToken.getValue().replace("\"", "\\\"")); // 转义
        System.out.println(loginToken.getValue());
        // 设置Cookie的过期时间，例如7天
        loginToken.setMaxAge(7 * 24 * 60 * 60); // 秒为单位
        // 设置Cookie的作用域，这里设置为整个域名
        loginToken.setPath("/");
        // 将Cookie添加到响应中
        response.addCookie(loginToken);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if (request == null) {
            return 0;
        }
        int result = userService.userLogout(request);
        return result;
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null || userObj.equals("logout")) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User currentUser = (User) userObj;
//        if (currentUser == null) {
//            throw new BusinessException(ErrorCode.NOT_LOGIN);
//        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("user", username);
        }
        return ResultUtils.success(userService.list(queryWrapper));
    }

    @GetMapping("/userList")
    public BaseResponse<Page<User>> userList(long pageSize,long pageNum,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("xiahua:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            System.out.println("get page from redis");
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 写缓存
        try {
            // 缓存过期时间为一分钟
            valueOperations.set(redisKey, userPage , 60000, TimeUnit.MILLISECONDS);
            System.out.println("redis reset cache");
        } catch (Exception e) {
            System.out.println("redis set key error");
        }
        return ResultUtils.success(userPage);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<User>> matchUsers(int num, HttpServletRequest request) {
        return ResultUtils.success(userService.recommendUsers(num, request));
    }
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/chat/start")
    public BaseResponse<Boolean> startChat(@RequestBody User ChatUser,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(!chatServer.isOpen()) chatServer.startServer(loginUser,ChatUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        System.out.println("update user");
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }



}
