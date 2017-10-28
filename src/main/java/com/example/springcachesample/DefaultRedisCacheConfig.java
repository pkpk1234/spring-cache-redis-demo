package com.example.springcachesample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis缓存默认配置 默认配置下:
 *   1. RedisTemplate使用JdkSerializationRedisSerializer将对象序列化并保存到
 *       redis中，这要求被缓存对象必须implements Serializable 接口
 *   2. 默认的keyGenerator对同名缓存下的无参数方法和入参相同的方法的缓存有问题，即不同的方法的key居然相同
 * 
 * @author 李佳明
 * @date 2017-10-28
 */
@Configuration
public class DefaultRedisCacheConfig {

	private final RedisTemplate redisTemplate;

	@Autowired
	public DefaultRedisCacheConfig(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		// 设置默认过期时间为60秒
		cacheManager.setDefaultExpiration(60);
		return cacheManager;
	}
}
