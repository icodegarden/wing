package io.github.icodegarden.wing.distribution.sync;

import java.util.Random;

import io.github.icodegarden.wing.distribution.sync.AbstractDistributionSyncStrategy;
import io.github.icodegarden.wing.distribution.sync.RedisBroadcast;
import io.github.icodegarden.wing.redis.JedisClusterCacherTests;
import io.github.icodegarden.wing.redis.JedisPoolCacherTests;
import io.github.icodegarden.wing.redis.RedisTemplateCacherTests;

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
