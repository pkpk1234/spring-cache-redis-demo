![spring boot cache redis使用注意点](https://pic3.zhimg.com/v2-572b45b308e954f3290ab85d6092af01_r.jpg)

# spring boot cache redis使用注意点

[![李佳明](https://pic3.zhimg.com/v2-47671bd2ed8b0230f83ad874b435af39_xs.jpg)](https://www.zhihu.com/people/li-jia-ming-70)[李佳明](https://www.zhihu.com/people/li-jia-ming-70)

<!-- react-empty: 33 -->

<time datetime="Sun Oct 29 2017 01:22:40 GMT+0800 (CST)">4 个月前</time>

今天同事[晓风轻](https://www.zhihu.com/people/xiaofengqing/activities)找我讨论了一下redis作为缓存，需要注意的点大致如下：

这里使用spring boot提供的redis starter，引入如下依赖

```language-xml
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
     <version>1.5.3.RELEASE</version>
 </dependency>
```

这个starter会使用RedisAutoConfiguration自动配置最基本的redis组件，关键点如下：

```language-java
...省略其他
//如果用户没有配置RedisTemplate则构造一个使用默认配置的RedisTemplate
@Bean
@ConditionalOnMissingBean(name = {"redisTemplate"})
public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
    RedisTemplate<Object, Object> template = new RedisTemplate();
     template.setConnectionFactory(redisConnectionFactory);
    return template;
}  

@Bean
@ConditionalOnMissingBean({RedisConnectionFactory.class})
public JedisConnectionFactory redisConnectionFactory() throws UnknownHostException {
    return this.applyProperties(this.createJedisConnectionFactory());
}      
...省略其他
```

同时这个starter还会触发org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration进行自动化配置，核心代码如下：

```language-java
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnMissingBean(CacheManager.class)
@Conditional(CacheCondition.class)
class RedisCacheConfiguration {
...省略其他
    //当用户未配置RedisCacheManager时，使用默认配置构造一个RedisCacheManager
	@Bean
	public RedisCacheManager cacheManager(RedisTemplate<Object, Object> redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		cacheManager.setUsePrefix(true);
		List<String> cacheNames = this.cacheProperties.getCacheNames();
		if (!cacheNames.isEmpty()) {
			cacheManager.setCacheNames(cacheNames);
		}
		return this.customizerInvoker.customize(cacheManager);
	}
...省略其他
}
```

此时开发者不需要任何配置，则可以使用spring redis 缓存数据了，但是此时的配置全部是默认的配置。

## 添加缓存默认超时配置

RedisCacheManager的setDefaultExpiration(long defaultExpireSeconds)可以配置缓存的默认超时时间，单位为秒。超时之后，redis自动删除该缓存。

可以通过自行返回配置了默认超时的RedisCacheManager实现该功能。如下：

```language-java
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
```

可以在redis中通过 ttl 命令查看超时时间，返回值单位为秒。

## 设置KeyGenerator

默认情况下，key值是有问题，当方法入参相同时，key值也相同，这样会造成不同的方法读取相同的缓存，从而造成异常。

可以通过extends CachingConfigurerSupport，并覆盖keyGenerator方法配置自定义的KeyGenerator。如下：

```language-java
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
        //设置key的序列化方式为String
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        // 设置默认过期时间为60秒
        cacheManager.setDefaultExpiration(60);
        return cacheManager;
     }

    /**
     * key值为className+methodName+参数值列表
     * @return
     */
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
```

此时key值如下

<figure>![](https://pic3.zhimg.com/80/v2-deb68124458521b1cb36daf38c9ea621_hd.jpg)</figure>

## 设置value的序列化方式

默认情况下RedisTemplate使用的序列化方式是JDK自带的序列化方式，实现类是org.springframework.data.redis.serializer.JdkSerializationRedisSerializer。

使用该方式，保存在redis中的值人类是无法阅读的。如下：

"\xac\xed\x00\x05sr\x00\x13java.util.ArrayListx\x81\xd2\x1d\x99\xc7a\x9d\x03\x00\x01I\x00\x04sizexp\x00\x00\x00\x05w\x04\x00\x00\x00\x05sr\x00*com.example.springcachesample.model.Person\x99\x9c\xe3\xc4\xf3E$\xa3\x02\x00\x04I\x00\x03ageL\x00\tfirstNamet\x00\x12Ljava/lang/String;L\x00\blastNameq\x00~\x00\x03L\x00\bpersonIdt\x00\x10Ljava/lang/Long;xp\x00\x00\x00\x14t\x00\x02f1t\x00\x02l1sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x00\x00\x00\x00\x00\x01sq\x00~\x00\x02\x00\x00\x00\x14t\x00\x02f2t\x00\x02l2sq\x00~\x00\b\x00\x00\x00\x00\x00\x00\x00\x02sq\x00~\x00\x02\x00\x00\x00\x14t\x00\x02f3t\x00\x02l3sq\x00~\x00\b\x00\x00\x00\x00\x00\x00\x00\x03sq\x00~\x00\x02\x00\x00\x00\x14t\x00\x02f4t\x00\x02l4sq\x00~\x00\b\x00\x00\x00\x00\x00\x00\x00\x04sq\x00~\x00\x02\x00\x00\x00\x14t\x00\x02f5t\x00\x02l5sq\x00~\x00\b\x00\x00\x00\x00\x00\x00\x00\x05x"

并且该Serializer要求被目标类必须实现Serializable接口。

当前比较常用的非二进制序列化方式为json。Spring提供了GenericJackson2JsonRedisSerializer专门用于json的序列化，可以通过RedisTemplate的setValueSerializer方法进行设置。如下：

```language-java
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
...省略其他
```

## 设置了@Cacheable的key时

当设置了@Cacheable的key时，spring不会使用用户配置的KeyGenerator进行key的生成，而是直接使用注解中的可以，此时需要保证不同方法的key的唯一性。

解决方法是在key中添加方法名称和参数值，如下

```language-java
...省略其他
@Cacheable(cacheNames = "personCache", key = "{#root.methodName,#id}")
    public Person getPersonById(Long id) {
...省略其他
```

## 其他

同一个缓存下的所有key值，在redis中会保存在名为 缓存名~keys的zset中。

缓存的value值则保存在string中。

## 代码

完整代码:[https://github.com/pkpk1234/spring-cache-redis-demo](https://link.zhihu.com/?target=https%3A//github.com/pkpk1234/spring-cache-redis-demo)

运行时通过 --spring.profiles.active= <profile>选择不同的配置

--spring.profiles.active= mykey 使用默认序列化和自定义key

--spring.profiles.active= json 使用json序列化和自定义key

不加--spring.profiles.active参数则全部使用默认值
