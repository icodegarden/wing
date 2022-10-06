package io.github.icodegarden.wing.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.KeySizeLRUCacher;
import io.github.icodegarden.wing.java.DefaultDirectMemoryCacher;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.level.GetOfUpgradeStrategy.UpgradeGtMinAvgDeleteInCurrent;
import io.github.icodegarden.wing.level.GetOfUpgradeStrategy.UpgradeGtMinAvgNotDeleteInDistributed;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;
import io.github.icodegarden.wing.redis.RedisCacher;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class GetOfUpgradeStrategyTests {

	Cacher L1 = new KeySizeLRUCacher(new KeySizeMetricsCacher(new HeapMemoryCacher()),1);//L1只有1个空间
	Cacher L2 = new KeySizeLRUCacher(new KeySizeMetricsCacher(new DefaultDirectMemoryCacher()),10);
	
	JedisPool jedisPool = new JedisPool(new GenericObjectPoolConfig(),"172.22.122.23",6399,2000,null);
	RedisExecutor redisExecutor = new PoolRedisExecutor(jedisPool);
	Cacher L3 = new KeySizeLRUCacher(new KeySizeMetricsCacher(new RedisCacher(redisExecutor)),10);
	
	Level level = Level.of(Arrays.asList(L1,L2,L3));
	
	String key = "key";
	String v = "v";
	String key2 = "key2";
	String v2 = "v2";
	String key3 = "key3";
	String v3 = "v3";
	int expireSeconds = 10;
	
	@BeforeEach
	public void before() {
	}
	
	/**
	 * 测试L3有缓存需要升级，达到条件，上升到L2，L3（DistributedCacher）中不会删
	 */
	@Test
	public void testUpgradeGtMinAvgNotDeleteDistributedCacher() throws Exception {
		OutOfLimitStrategy outOfLimitStrategy = new OutOfLimitStrategy.ToNextLevel();
		
		L3.set(key, v, expireSeconds);
		L3.set(key2, v2, expireSeconds);
		
		L3.get(key);
		L3.get(key);//2次
		//平均1次，达到条件
		long minUsedTimes = 1;
		
		UpgradeGtMinAvgNotDeleteInDistributed strategy = new GetOfUpgradeStrategy.UpgradeGtMinAvgNotDeleteInDistributed(minUsedTimes);
		strategy.upgrade(level.getNext().getNext()/*==L3*/, key, v, outOfLimitStrategy);
		
		assertEquals(v, L2.get(key));//v上升到了L2
		assertEquals(v, L3.get(key));//v在L3还是存在的
	}
	
	/**
	 * 测试L2有缓存需要升级，达到条件，上升到L1，L1的下降到L2
	 */
	@Test
	public void testUpgradeGtMinAvgAndDelete() throws Exception {
		OutOfLimitStrategy outOfLimitStrategy = new OutOfLimitStrategy.ToNextLevel();
		
		L1.set(key3, v3, expireSeconds);//L1先有1个key3，占满空间
		
		L2.set(key, v, expireSeconds);
		L2.set(key2, v2, expireSeconds);
		
		L2.get(key);
		L2.get(key);//2次
		//平均1次，达到条件
		long minUsedTimes = 1;
		
		assertNull(L1.get(key));//上升前L1没有
		
		UpgradeGtMinAvgNotDeleteInDistributed strategy = new GetOfUpgradeStrategy.UpgradeGtMinAvgNotDeleteInDistributed(minUsedTimes);
		strategy.upgrade(level.getNext()/*==L2*/, key, v, outOfLimitStrategy);
		
		assertEquals(v, L1.get(key));//v上升到了L1
		assertNull(L2.get(key));//v在L2也被删了
		assertEquals(v3, L2.get(key3));//下降到了L2
	}
	
	@Test
	public void testUpgradeGtMinAvgThenDeleteInCurrent() throws Exception {
		OutOfLimitStrategy outOfLimitStrategy = new OutOfLimitStrategy.ToNextLevel();
		
		L2.set(key, v, expireSeconds);
		L2.set(key2, v2, expireSeconds);
		
		L2.get(key);
		L2.get(key);//2次
		//平均1次，达到条件
		long minUsedTimes = 1;
		
		UpgradeGtMinAvgDeleteInCurrent u1 = new GetOfUpgradeStrategy.UpgradeGtMinAvgDeleteInCurrent(minUsedTimes);
		u1.upgrade(level.getNext()/*L2*/, key, v, outOfLimitStrategy);
		
		assertEquals(v, L1.get(key));//v上升到了L1
		assertNull(L2.get(key));//v在L2不存在了
	}
}
