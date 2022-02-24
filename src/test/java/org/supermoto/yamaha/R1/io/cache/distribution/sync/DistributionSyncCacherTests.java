package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;

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
public class DistributionSyncCacherTests {
	
	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected int expireSeconds = 10;
	
	NioBroadCastTests nioBroadCastTests = new NioBroadCastTests();
	
	DistributionSyncStrategy newInstance() {
		return nioBroadCastTests.newInstance();
	}
	
	@Test
	public void testOnSet() throws Exception {
		DistributionSyncStrategy strategy1 = newInstance();
		Cacher c1 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d1 = new DistributionSyncCacher("applicationName", c1, strategy1);
		
		DistributionSyncStrategy strategy2 = newInstance();
		Cacher c2 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d2 = new DistributionSyncCacher("applicationName", c2, strategy2);
		
		DistributionSyncStrategy strategy3 = newInstance();
		Cacher c3 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d3 = new DistributionSyncCacher("applicationName", c3, strategy3);
		
		DistributionSyncStrategy strategy4 = newInstance();
		Cacher c4 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d4 = new DistributionSyncCacher("applicationName", c4, strategy4);
		
		Thread.sleep(100);//等待完成订阅
		
		assertNull(d1.get(key));
		assertNull(d2.get(key));
		assertNull(d3.get(key));
		assertNull(d4.get(key));
		
		d1.set(key, v, expireSeconds);
		
		Thread.sleep(1000);//等待同步的过程
		
		assertNotNull(d1.get(key));
		assertNotNull(d2.get(key));
		assertNotNull(d3.get(key));
		assertNotNull(d4.get(key));
		
		strategy1.close();
		strategy2.close();
		strategy3.close();
		strategy4.close();
	}
	
	@Test
	public void testOnRemove() throws Exception {
		DistributionSyncStrategy strategy1 = newInstance();
		Cacher c1 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d1 = new DistributionSyncCacher("applicationName", c1, strategy1);
		
		DistributionSyncStrategy strategy2 = newInstance();
		Cacher c2 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d2 = new DistributionSyncCacher("applicationName", c2, strategy2);
		
		DistributionSyncStrategy strategy3 = newInstance();
		Cacher c3 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d3 = new DistributionSyncCacher("applicationName", c3, strategy3);
		
		DistributionSyncStrategy strategy4 = newInstance();
		Cacher c4 = spy(HeapMemoryCacher.class);
		DistributionSyncCacher d4 = new DistributionSyncCacher("applicationName", c4, strategy4);
		
		d1.set(key, v, expireSeconds);
		d2.set(key, v, expireSeconds);
		d3.set(key, v, expireSeconds);
		d4.set(key, v, expireSeconds);
		
		assertNotNull(d1.get(key));
		assertNotNull(d2.get(key));
		assertNotNull(d3.get(key));
		assertNotNull(d4.get(key));
		
		Thread.sleep(100);//等待完成订阅
		
		d1.remove(key);
		
		Thread.sleep(1000);//等待同步的过程
		
		assertNull(d1.get(key));
		assertNull(d2.get(key));
		assertNull(d3.get(key));
		assertNull(d4.get(key));
		
		strategy1.close();
		strategy2.close();
		strategy3.close();
		strategy4.close();
	}
}
