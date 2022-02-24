package io.github.icodegarden.wing.performance;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.redis.JedisPoolCacherTests;
import io.github.icodegarden.wing.redis.RedisCacher;

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
