package io.github.icodegarden.wing.redis;

import java.io.Serializable;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.TemplateRedisExecutor;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.redis.RedisCacher;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RedisTemplateCacherTests extends RedisCacherTests {

	Cacher cacher;
	
	static RedisConnectionFactory connectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		JedisClientConfiguration clientConfig = JedisClientConfiguration.builder().usePooling().poolConfig(poolConfig)
				.build();

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName("172.22.122.23");
//		redisConfig.setPassword(RedisPassword.of("8q9P&ZF5SQ@Fv49x"));
		redisConfig.setPort(6399);

		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisConfig, clientConfig);
		jedisConnectionFactory.afterPropertiesSet();//需要调用一下，不然pool不会生效，内部总是创建一个新链接
		return jedisConnectionFactory;
	}
	
	@Override
	protected Cacher getCacher() {
		RedisExecutor redisExecutor = new TemplateRedisExecutor(newRedisTemplate());
		cacher = new RedisCacher(redisExecutor, new KryoSerializer(), new KryoDeserializer());
		return cacher;
	}
	
	public static RedisTemplate<String, Serializable> newRedisTemplate(){
		RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<String, Serializable>();
		redisTemplate.setConnectionFactory(connectionFactory());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
}
