package org.supermoto.yamaha.R1.io.cache.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.supermoto.yamaha.R1.io.cache.Cacher;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
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
		cacher = RedisCacher.jedisPool(newJedisPool(),  new KryoSerializer(), new KryoDeserializer());
		return cacher;
	}
	
	public static JedisPool newJedisPool() {
		JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(),"172.22.122.23",6399,2000,null);
		return jedisPool;
	}
}
