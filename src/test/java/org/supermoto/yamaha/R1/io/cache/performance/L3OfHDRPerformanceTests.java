package org.supermoto.yamaha.R1.io.cache.performance;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.level.LevelableCacher;
import org.supermoto.yamaha.R1.io.cache.level.SetOfFromStrategy;
import org.supermoto.yamaha.R1.io.cache.redis.RedisCacher;
import org.supermoto.yamaha.R1.io.cache.usage.UsageBuilder;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
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
	RedisCacher redisCacher = RedisCacher.jedisPool(jedisPool, serializer,deserializer );
	
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
