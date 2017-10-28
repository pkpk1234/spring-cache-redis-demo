package com.example.springcachesample;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;

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
