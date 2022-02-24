package io.github.icodegarden.wing.redis;

import io.github.icodegarden.wing.redis.ClusterRedisExecutor;
import io.github.icodegarden.wing.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class ClusterRedisExecutorTests extends RedisExecutorTests {
	@Override
	protected RedisExecutor newInstance() {
		return new ClusterRedisExecutor(JedisClusterCacherTests.newJedisCluster());
	}
	
}
