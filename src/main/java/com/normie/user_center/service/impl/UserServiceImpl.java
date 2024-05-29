package com.normie.user_center.service.impl;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.normie.user_center.common.ErrorCode;
import com.normie.user_center.exception.BusinessException;
import com.normie.user_center.model.User;
import com.normie.user_center.mapper.UserMapper;
import com.normie.user_center.service.UserService;
import com.normie.user_center.utils.AlgorithmUtils;
import jdk.jfr.Description;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.normie.user_center.constant.UserConstant.*;

/**
* @author 10377
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-03-08 20:24:00
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;
    private static final String SALT = "normie";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        System.out.println("用户尝试注册");
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return -1;
        }
        if (userAccount.length() < 4) {
            return -1;
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }

        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            return -1;
        }
        if(!userPassword.equals(checkPassword)) {
            return -1;
        }

        // 查询数据库 不能有相同的账户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            return -1;
        }

        String encryptPassword = DigestUtils.md5DigestAsHex(((SALT+userPassword).getBytes()));
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUsername(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            return -1;
        }
        return 0;
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }

        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            return null;
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询数据库 核对用户名和密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user == null) {
            log.info("user login failed");
            return null;
        }
        log.info("user login success");
        // 用户脱敏
        User safeUser = getSafetyUser(user);
        //记录用户的登录态
        HttpSession session = request.getSession();
        session.setAttribute(USER_LOGIN_STATE, safeUser);
        session.setMaxInactiveInterval(24 * 60 * 60 * 100);

        return safeUser;
    }

    @Override
    public Integer userLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute(USER_LOGIN_STATE,"logout");
        return 1;
    }

    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> users = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();

        return users.stream().filter(user -> {
            String tags = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>(){}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public Integer updateUser(User user, User loginUser) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 只有管理员或者本人可以修改
        if (!isAdmin(loginUser) && !loginUser.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Long userId = user.getId();
        if(userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userMapper.updateById(user);
        return result;
    }

    @Override
    public boolean isAdmin(User user) {
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        if (user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(user.equals("logout")){
            return null;
        }
        return (User) user ;
    }

    @Override
    public List<User> recommendUsers(int num, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = userMapper.selectList(queryWrapper);

        Gson gson = new Gson();
        Map<Long, List<String>> userTags = new HashMap<>();
        // 先插入收到推荐的用户
        Long loginUserId = loginUser.getId();
        String tags = loginUser.getTags();
        userTags.put(loginUserId,gson.fromJson(tags, new TypeToken<List<String>>() {}.getType()));
        // 插入其他用户
        for (User user : userList) {
            userTags.put(user.getId(),gson.fromJson(user.getTags(), new TypeToken<List<String>>() {}.getType()));
        }

        // 生成标签全集
        Set<String> allTagsSet = new HashSet<>();
        for (List<String> tag : userTags.values()) {
            allTagsSet.addAll(tag);
        }
        List<String> allTags = new ArrayList<>(allTagsSet);

        // 将用户标签列表向量化
        Map<Long, int[]> userVectors = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : userTags.entrySet()) {
            if (entry.getValue() != null) {
                userVectors.put(entry.getKey(), AlgorithmUtils.vectorizeTags(entry.getValue(), allTags));
            }
        }
        // 最后进行余弦相似度算法推荐用户
        List<Long> recommendations = AlgorithmUtils.recommendUsers(loginUserId, userVectors,num);
        System.out.println("Recommendations for " + loginUserId + ": " + recommendations);

        return userMapper.selectList(new QueryWrapper<User>().in("id", recommendations));
    }

    @Description("sql搜索")
    public List<User> searchUsersByTagsSql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }


}




