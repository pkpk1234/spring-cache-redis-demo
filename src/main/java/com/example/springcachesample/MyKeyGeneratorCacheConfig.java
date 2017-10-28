package com.example.springcachesample;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Profile("mykey")
@Configuration
public class MyKeyGeneratorCacheConfig extends CachingConfigurerSupport {

	private final RedisTemplate redisTemplate;

	@Autowired
	public MyKeyGeneratorCacheConfig(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;

	}

	@Override
	public CacheManager cacheManager() {
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		// 设置默认过期时间为60秒
		cacheManager.setDefaultExpiration(60);
		return cacheManager;
	}

	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object o, Method method, Object... args) {
				StringBuilder sb = new StringBuilder();
				sb.append(o.getClass().getName()).append("#");
				sb.append(method.getName()).append("(");
				for (Object obj : args) {
					sb.append(obj.toString()).append(",");
				}
				sb.append(")");
				return sb.toString();
			}

		};
	}

}
