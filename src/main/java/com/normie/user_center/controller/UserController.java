package com.normie.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.normie.user_center.common.BaseResponse;
import com.normie.user_center.common.ErrorCode;
import com.normie.user_center.common.ResultUtils;
import com.normie.user_center.exception.BusinessException;
import com.normie.user_center.model.User;
import com.normie.user_center.model.request.UserLoginRequest;
import com.normie.user_center.model.request.UserRegisterRequest;
import com.normie.user_center.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.normie.user_center.constant.UserConstant.ADMIN_ROLE;
import static com.normie.user_center.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = { "http://localhost:3000" } )
public class UserController {
    @Resource
    private UserService userService;

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
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        System.out.println("尝试登录");
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        return ResultUtils.success(userService.userLogin(userAccount, userPassword, request));
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
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
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

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> userList(long pageSize,long pageNum,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("xhMatch:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(10, 10), queryWrapper);
        // 写缓存
        try {
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            System.out.println("redis set key error");
        }
        return ResultUtils.success(userPage);
    }
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }



    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }



}
