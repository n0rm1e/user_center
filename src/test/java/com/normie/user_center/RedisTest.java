package com.normie.user_center;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @org.junit.jupiter.api.Test
    public void test1(){
        redisTemplate.opsForValue().set("name","lisi");
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}
