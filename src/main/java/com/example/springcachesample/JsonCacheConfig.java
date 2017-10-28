package com.example.springcachesample;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Profile("json")
@Configuration
public class JsonCacheConfig extends CachingConfigurerSupport {

	@Bean
	public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate redisTemplate = new RedisTemplate();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
		redisTemplate.setKeySerializer(new MyStringRedisSerializer());
		redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);
		return redisTemplate;
	}

	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		// 设置默认过期时间为60秒
		cacheManager.setDefaultExpiration(60);
		Map<String, Long> map = new HashMap<>();
		// 设置demo~keys过期时间为5秒
		map.put("demo~keys", 5L);
		cacheManager.setExpires(map);
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
