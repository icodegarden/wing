package org.supermoto.yamaha.R1.io.cache.performance;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.redis.JedisPoolCacherTests;
import org.supermoto.yamaha.R1.io.cache.redis.RedisCacher;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisPoolCacherPerformanceTests extends PerformanceTests  {

	@Override
	protected Cacher newCacher() {
		return RedisCacher.jedisPool(JedisPoolCacherTests.newJedisPool(),  new KryoSerializer(), new KryoDeserializer());
	}
	
	@Override
	protected String name() {
		return "RedisCacher.jedisPool";
	}
}
