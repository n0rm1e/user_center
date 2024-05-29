package com.normie.user_center;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;
    @Test
    public void test() {
        RList<String> list = redissonClient.getList("test-list");
        list.add("nihao");
        System.out.println(list.get(0));
        list.remove(0);

    }
}
