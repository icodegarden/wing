package io.github.icodegarden.wing.performance;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.level.LevelableCacher;
import io.github.icodegarden.wing.level.SetOfFromStrategy;
import io.github.icodegarden.wing.redis.JedisPoolCacherTests;
import io.github.icodegarden.wing.redis.RedisCacher;
import io.github.icodegarden.wing.usage.UsageBuilder;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class L3OfHDRPerformanceTests extends PerformanceTests  {

	KryoSerializer serializer = new KryoSerializer();
	KryoDeserializer deserializer = new KryoDeserializer();
	
	JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(),"172.22.122.23",6399,2000,null);
	RedisExecutor redisExecutor = new PoolRedisExecutor(JedisPoolCacherTests.newJedisPool());
	RedisCacher redisCacher = new RedisCacher(redisExecutor, serializer,deserializer );
	
	@Override
	protected Cacher newCacher() {
		LevelableCacher levelableCacher = UsageBuilder.redisOfL3BasedOnHeapAndDirectBuilder().redisCacher(redisCacher)
				.serializer(serializer).deserializer(deserializer).setOfFrom(new SetOfFromStrategy.Lowest()).build();
		
		return levelableCacher;
	}
	
	@Override
	protected String name() {
		return "redisOfL3BasedOnHeapAndDirect";
	}
}
