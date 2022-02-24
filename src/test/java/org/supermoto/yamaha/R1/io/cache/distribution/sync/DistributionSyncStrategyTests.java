package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.distribution.sync.DistributionSyncCacher;
import org.supermoto.yamaha.R1.io.cache.distribution.sync.DistributionSyncStrategy;
import org.supermoto.yamaha.R1.io.cache.java.HeapMemoryCacher;

import io.cache.UserForTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class DistributionSyncStrategyTests {
	
	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected int expireSeconds = 10;
	
	protected abstract DistributionSyncStrategy newInstance();
	
	@Test
	public void testOnSet() throws Exception {
		DistributionSyncStrategy strategy1 = newInstance();
		Cacher c1 = spy(HeapMemoryCacher.class);
		strategy1.injectCacher(new DistributionSyncCacher("applicationName", c1, strategy1));
		
		DistributionSyncStrategy strategy2 = newInstance();
		Cacher c2 = spy(HeapMemoryCacher.class);
		strategy2.injectCacher(new DistributionSyncCacher("applicationName", c2, strategy2));
		
		DistributionSyncStrategy strategy3 = newInstance();
		Cacher c3 = spy(HeapMemoryCacher.class);
		strategy3.injectCacher(new DistributionSyncCacher("applicationName", c3, strategy3));
		
		DistributionSyncStrategy strategy4 = newInstance();
		Cacher c4 = spy(HeapMemoryCacher.class);
		strategy4.injectCacher(new DistributionSyncCacher("applicationName", c4, strategy4));
		
		Thread.sleep(100);//等待完成订阅
		
		strategy1.onSet(key, v, expireSeconds);
		
		Thread.sleep(1000);//等待同步的过程
		
		verify(c1,times(0)).set(eq(key), eq(v), eq(expireSeconds));
		verify(c2,times(1)).set(eq(key), eq(v), eq(expireSeconds));
		verify(c3,times(1)).set(eq(key), eq(v), eq(expireSeconds));
		verify(c4,times(1)).set(eq(key), eq(v), eq(expireSeconds));
		
		assertNull(c1.get(key));
		assertNotNull(c2.get(key));
		assertNotNull(c3.get(key));
		assertNotNull(c4.get(key));
		
		strategy1.close();
		strategy2.close();
		strategy3.close();
		strategy4.close();
	}
	
	@Test
	public void testOnRemove() throws Exception {
		DistributionSyncStrategy strategy1 = newInstance();
		Cacher c1 = spy(HeapMemoryCacher.class);
		strategy1.injectCacher(new DistributionSyncCacher("applicationName", c1, strategy1));
		
		DistributionSyncStrategy strategy2 = newInstance();
		Cacher c2 = spy(HeapMemoryCacher.class);
		strategy2.injectCacher(new DistributionSyncCacher("applicationName", c2, strategy2));
		
		DistributionSyncStrategy strategy3 = newInstance();
		Cacher c3 = spy(HeapMemoryCacher.class);
		strategy3.injectCacher(new DistributionSyncCacher("applicationName", c3, strategy3));
		
		DistributionSyncStrategy strategy4 = newInstance();
		Cacher c4 = spy(HeapMemoryCacher.class);
		strategy4.injectCacher(new DistributionSyncCacher("applicationName", c4, strategy4));
		
		c1.set(key, v, expireSeconds);
		c2.set(key, v, expireSeconds);
		c3.set(key, v, expireSeconds);
		c4.set(key, v, expireSeconds);
		
		Thread.sleep(1000);//等待完成订阅
		
		strategy1.onRemove(key);
		
		Thread.sleep(1000);//等待同步的过程
		
		verify(c1,times(0)).remove(eq(key));
		verify(c2,times(1)).remove(eq(key));
		verify(c3,times(1)).remove(eq(key));
		verify(c4,times(1)).remove(eq(key));
		
		assertNotNull(c1.get(key));
		assertNull(c2.get(key));
		assertNull(c3.get(key));
		assertNull(c4.get(key));
		
		strategy1.close();
		strategy2.close();
		strategy3.close();
		strategy4.close();
	}
}
