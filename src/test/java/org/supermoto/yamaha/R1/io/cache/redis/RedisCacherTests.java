package org.supermoto.yamaha.R1.io.cache.redis;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.CacherTests;
import org.supermoto.yamaha.R1.io.cache.distribution.DistributedCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class RedisCacherTests extends CacherTests {

	@Override
	protected boolean notnullOnRemoveIfExists() {
		return false;
	}
	
	@BeforeEach
	public void before() {
		key = "{foo}.key";
		key2 = "{foo}.key2";
		getCacher().remove(Arrays.asList(key,key2));//每个开始前需要清理下
	}
	
	@Test
	public void testinstanceOf() throws Exception {
		Cacher cacher = getCacher();
		assertTrue(cacher.instanceOf(DistributedCacher.class));
	}
}
