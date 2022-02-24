package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import java.util.Random;

import org.supermoto.yamaha.R1.io.cache.distribution.sync.AbstractDistributionSyncStrategy;
import org.supermoto.yamaha.R1.io.cache.distribution.sync.RedisBroadcast;
import org.supermoto.yamaha.R1.io.cache.redis.JedisClusterCacherTests;
import org.supermoto.yamaha.R1.io.cache.redis.JedisPoolCacherTests;
import org.supermoto.yamaha.R1.io.cache.redis.RedisTemplateCacherTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RedisBroadCastTests extends DistributionSyncStrategyTests {

	@Override
	protected AbstractDistributionSyncStrategy newInstance() {
		RedisBroadcast[] arr = { RedisBroadcast.jedisPool(JedisPoolCacherTests.newJedisPool()),
				RedisBroadcast.jedisCluster(JedisClusterCacherTests.newJedisCluster()),
				RedisBroadcast.redisTemplate(RedisTemplateCacherTests.newRedisTemplate()) };
		return arr[new Random().nextInt(3)];
		
//		return RedisBroadcast.jedisPool(JedisPoolCacherTests.newJedisPool()); 
//		return RedisBroadcast.jedisCluster(JedisClusterCacherTests.newJedisCluster()); 
//		return RedisBroadcast.redisTemplate(RedisTemplateCacherTests.newRedisTemplate()); 
	}

}
