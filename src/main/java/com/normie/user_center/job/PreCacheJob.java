package com.normie.user_center.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.normie.user_center.model.User;
import com.normie.user_center.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预热缓存
 */
public class PreCacheJob {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserService userService;
    // 缓存用户列表
    private List<Long> primaryUserList;
    // 每日0点预热缓存
    @Scheduled(cron = "0 0 0 * * ?")
    public void preCache() {
        RLock lock = redissonClient.getLock("anyLockName");
        try {
            // 尝试获取锁，如果获取不到则等待直到获取到锁或者超时
            boolean isLocked = lock.tryLock(10, TimeUnit.SECONDS); // 尝试加锁，等待最多10秒
            if (isLocked) {
                // 执行临界区代码
                System.out.println("Lock acquired, doing some critical work...");

                // 预热缓存
                for (long userId: primaryUserList) {
                    String redisKey = String.format("xiahua:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    // 缓存过期时间为一周
                    valueOperations.set(redisKey, userPage , 1, TimeUnit.DAYS);
                }
            } else {
                System.out.println("Unable to acquire lock.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted while trying to acquire lock.");
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("Lock released.");
            }
        }


    }
}
