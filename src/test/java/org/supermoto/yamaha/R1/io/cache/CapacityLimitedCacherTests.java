package org.supermoto.yamaha.R1.io.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.CapacityLimitedCacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import org.supermoto.yamaha.R1.io.cache.java.HeapMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.metrics.KeySizeMetricsCacher;
import org.supermoto.yamaha.R1.io.cache.metrics.MetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class CapacityLimitedCacherTests {

	MetricsCacher L1 = new KeySizeMetricsCacher(new HeapMemoryCacher());
	
	String key = "key";
	String v = "v";
	int expireSeconds = 10;
	
	TestCapacityLimitedCacher cacher;
	
	@BeforeEach
	public void before() {
		cacher = new TestCapacityLimitedCacher(L1);	
	}
	
	private class TestCapacityLimitedCacher extends CapacityLimitedCacher{
		public TestCapacityLimitedCacher(MetricsCacher cacher) {
			super(cacher);
		}
		@Override
		protected <V> List<Tuple3<String, Object, Integer>> removePreSet(List<Tuple3<String, V, Integer>> kvts) {
			return null;
		}
	}
	private class TestCapacityLimitedCacher2 extends CapacityLimitedCacher{
		public TestCapacityLimitedCacher2(MetricsCacher cacher) {
			super(cacher);
		}
		@Override
		protected <V> List<Tuple3<String, Object, Integer>> removePreSet(List<Tuple3<String, V, Integer>> kvts) {
			return null;
		}
	}
	/**
	 * 简单的set，触发L1的set
	 */
	@Test
	public void testSet() throws Exception {
		cacher.set(key, v, expireSeconds);
		
		Object get = cacher.get(key);
		assertEquals(v, get);
	}
	/**
	 * 基于testusedTimesAfterSet，再remove，usedTimes变为0
	 */
	@Test
	public void testRemove() throws Exception {
		testusedTimesAfterSet();
		
		Tuple3<String, Object, Integer> remove = cacher.remove(key);
		assertNotNull(remove);
		assertEquals(expireSeconds, remove.getT3().intValue());
		
		assertEquals(0, cacher.getMetricsCacher().usedTimes(key));//因为已经remove了
	}
	/**
	 * 基于testusedTimesAfterSet，再remove，usedTimes变为0
	 */
	@Test
	public void testRemoves() throws Exception {
		testusedTimesAfterSet();
		
		List<Tuple3<String, Object, Integer>> removes = cacher.remove(Arrays.asList(key));
		
		assertEquals(1, removes.size());
		assertEquals(expireSeconds, removes.get(0).getT3().intValue());
		
		assertEquals(0, cacher.getMetricsCacher().usedTimes(key));//因为已经remove了
	}
	/**
	 * 在set前进行get，不会使usedTimes增加
	 */
	@Test
	public void testusedTimesBeforeSet() throws Exception {
		cacher.get(key);
		assertEquals(0, cacher.getMetricsCacher().usedTimes(key));
	}
	/**
	 * 在set后进行get，使usedTimes增加
	 */
	@Test
	public void testusedTimesAfterSet() throws Exception {
		cacher.set(key, v, expireSeconds);
		
		cacher.get(key);
		assertEquals(1, cacher.getMetricsCacher().usedTimes(key));
		cacher.get(key);//增加1次
		assertEquals(2, cacher.getMetricsCacher().usedTimes(key));
	}
}
