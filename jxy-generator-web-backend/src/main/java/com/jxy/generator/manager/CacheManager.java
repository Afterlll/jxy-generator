package com.jxy.generator.manager;

import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器
 */
@Component
public class CacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * 写缓存
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        // 添加或者更新一个缓存元素
        localCache.put(key, value);
        // 写入 redis 缓存
        redisTemplate.opsForValue().set(key, value, 100, TimeUnit.MINUTES);
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        // 先从本地缓存中尝试获取
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 本地缓存未命中，尝试从 redis 中获取
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            // 将从 redis 中获取的数据存进本地缓存
            localCache.put(key, value);
        }

        return value;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void delete(String key) {
        // 移除一个缓存元素
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }


}
