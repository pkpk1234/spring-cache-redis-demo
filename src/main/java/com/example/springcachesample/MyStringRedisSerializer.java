package com.example.springcachesample;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 自定义String序列化反序列化类，
 * 避免org.springframework.data.redis.serializer.StringRedisSerializer在传人参数不是String类型时抛出异常
 * @author 李佳明
 * @date 2017-10-28
 * @see org.springframework.data.redis.serializer.StringRedisSerializer
 */
public class MyStringRedisSerializer implements RedisSerializer<Object> {

	private final Charset charset;

	public MyStringRedisSerializer() {
		this(Charset.forName("UTF8"));
	}

	public MyStringRedisSerializer(Charset charset) {
		this.charset = charset;
	}

	@Override
	public String deserialize(byte[] bytes) {
		return (bytes == null ? null : new String(bytes, charset));
	}

	@Override
	public byte[] serialize(Object object) {
		String string = object.toString();
		return (string == null ? null : string.getBytes(charset));
	}
}
