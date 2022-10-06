package io.github.icodegarden.wing.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.redis.RedisCacher;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class JedisPoolCacherTests extends RedisCacherTests {

	Cacher cacher;
	
	@Override
	protected Cacher getCacher() {
		RedisExecutor redisExecutor = new PoolRedisExecutor(newJedisPool());
		cacher = new RedisCacher(redisExecutor,  new KryoSerializer(), new KryoDeserializer());
		return cacher;
	}
	
	public static JedisPool newJedisPool() {
		JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(),"172.22.122.23",6399,10000,null);
		return jedisPool;
	}
}
