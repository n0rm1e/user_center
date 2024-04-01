package com.normie.user_center.service;

import com.normie.user_center.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 10377
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-03-08 20:24:00
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return 新用户Id
     */

    long userRegister(String userAccount, String userPassword,String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount 账户
     * @param userPassword 密码
     * @param request Servlet请求
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户登出
     * @param request
     * @return 1:成功 0:失败
     */
    Integer userLogout(HttpServletRequest request);


    User getSafetyUser(User user);
}
