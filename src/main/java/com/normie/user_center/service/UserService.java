package com.normie.user_center.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.normie.user_center.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.normie.user_center.constant.UserConstant.ADMIN_ROLE;
import static com.normie.user_center.constant.UserConstant.USER_LOGIN_STATE;

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


    /**
     * 获取脱敏用户信息
     * @param user
     * @return
     */
    User getSafetyUser(User user);

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    Integer updateUser(User user, User loginUser);

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    boolean isAdmin(HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    /**
     * 匹配用户
     * @param num
     * @param request
     * @return
     */
    List<User> recommendUsers(int num, HttpServletRequest request);
}
