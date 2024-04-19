package com.normie.user_center;

import com.normie.user_center.model.User;
import com.normie.user_center.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;

@SpringBootTest
public class InsertUsers {
    @Resource
    UserService userService;
    @Test
    public void insertUsers() {

        ArrayList<User> users = new ArrayList<>();
        for (int i = 20000; i < 30000; i++) {
            User user = new User();
            user.setUsername("user#" + i);
            user.setUserAccount("user#" + i);
            user.setUserPassword("12345678");
            user.setAvatarUrl("");
            user.setGender(1);
            user.setPhone("");
            user.setEmail("");
            user.setUserStatus(0);
            user.setTags("[\"Ů\",\"linux\",\"python\"]");
            users.add(user);
        }
        userService.saveBatch(users,5000);
    }
}
