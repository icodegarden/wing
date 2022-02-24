package org.supermoto.yamaha.R1.io.cache.redis;

import org.supermoto.yamaha.R1.io.cache.redis.ClusterRedisExecutor;
import org.supermoto.yamaha.R1.io.cache.redis.RedisExecutor;

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
