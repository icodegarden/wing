package io.github.icodegarden.wing.usage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.PerformanceTests;
import io.github.icodegarden.wing.level.LevelableCacher;
import io.github.icodegarden.wing.redis.JedisPoolCacherTests;
import io.github.icodegarden.wing.redis.RedisCacher;
import io.github.icodegarden.wing.usage.UsageBuilder;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class L3OfHDRTests extends PerformanceTests {

	KryoSerializer serializer = new KryoSerializer();
	KryoDeserializer deserializer = new KryoDeserializer();
	
	JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(),"172.22.122.23",6399,2000,null);
	RedisExecutor redisExecutor = new PoolRedisExecutor(jedisPool);
	RedisCacher redisCacher = new RedisCacher(redisExecutor, serializer,deserializer );
	
	String key = "key";
	String v = "v";
	String key2 = "key2";
	String v2 = "v2";
	int expireSeconds = 10;
	
	LevelableCacher levelableCacher;
	
	@Override
	protected Cacher getCacher() {
		levelableCacher = UsageBuilder.redisOfL3BasedOnHeapAndDirectBuilder().redisCacher(redisCacher)
				.serializer(serializer).deserializer(deserializer).maxKeySizeOfHeap(1000).maxBytesOfDirect(2048000)
				.upgradeMinUsedTimes(1).build();
		
		return levelableCacher;
	}
	
	/**
	 * set v，验证在L3，不在L1、L2
	 * get 次数达到条件，验证在L2和L3都有，L1没有
	 * 继续 get 次数达到条件，验证在L1和L3都有、L2没有
	 */
	@Test
	public void testAll() throws Exception {
		levelableCacher.set(key, v, expireSeconds);
		/**
		 * set v，验证在L3，不在L1、L2
		 */
		Cacher L1 = levelableCacher.getLevel(1).getCacher();
		Cacher L2 = levelableCacher.getLevel(2).getCacher();
		Cacher L3 = levelableCacher.getLevel(3).getCacher();

		assertEquals(v, L3.get(key));//直接L3get不会上升的
		assertNull(L1.get(key));
		assertNull(L2.get(key));
		
		/**
		 * get 次数达到条件，验证在L2和L3都有，L1没有
		 */
		levelableCacher.get(key);//促使上升
		assertEquals(v, L2.get(key));
		assertEquals(v, L3.get(key));
		
		/**
		 * 继续 get 次数达到条件，验证在L1和L3都有、L2没有
		 */
		levelableCacher.get(key);//促使上升
		assertEquals(v, L1.get(key));
		assertEquals(v, L3.get(key));
		assertNull(L2.get(key));
	}
	
}
