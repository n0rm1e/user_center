package com.normie.user_center.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.normie.user_center.model.User;
import com.normie.user_center.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * 用户服务测试
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUserAccount("12345");
        user.setUsername("tester");
        user.setAvatarUrl("https://space.bilibili.com/10131208?spm_id_from=333.1007.0.0");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123");
        user.setEmail("456");

        boolean result = userService.save(user);

        System.out.println(user.getId());
        Assertions.assertTrue(result);

    }
    @Test
    public void testRegisterUser() {
        long result = userService.userRegister("oreax", "12345678", "12345678");
        System.out.println(result);
    }

    @Test
    public void testTagSearch() {
        List<String> tagNameList = new ArrayList<>();
        tagNameList.add("java");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        System.out.println(userList);
    }

}