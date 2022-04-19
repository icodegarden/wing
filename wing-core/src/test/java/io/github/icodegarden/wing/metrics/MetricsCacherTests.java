package io.github.icodegarden.wing.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.UserForTests;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;
import io.github.icodegarden.wing.metrics.MetricsCacher;


/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class MetricsCacherTests {

	String key = "key";
	UserForTests v = new UserForTests("name", 18);
	String key2 = "key2";
	UserForTests v2 = new UserForTests("name2", 28);
	int expireSeconds = 10;
	
	Cacher L1 = new HeapMemoryCacher();
	
	TestMetricsCacher1 cacher = new TestMetricsCacher1(L1);	
	
	@BeforeEach
	public void before() {
	}
	
	private class TestMetricsCacher1 extends MetricsCacher{
		public TestMetricsCacher1(Cacher cacher) {
			super(cacher);
		}
	}
	private class TestMetricsCacher2 extends MetricsCacher{
		public TestMetricsCacher2(Cacher cacher) {
			super(cacher);
		}
	}
	
	/**
	 * 使用不同引用
	 */
	@Test
	public void testNew() throws Exception {
		TestMetricsCacher2 cacher2 = new TestMetricsCacher2(L1);
		
		cacher2.set(key, v, expireSeconds);
		cacher2.get(key);//使用1次
		
		assertEquals(1, cacher2.usedTimes(key));//1
		assertEquals(0, cacher.usedTimes(key));//0
	}
	/**
	 * 使用同一个引用
	 */
	@Test
	public void testNewMultiplex() throws Exception {
		TestMetricsCacher2 cacher2 = new TestMetricsCacher2(cacher);
		
		cacher2.set(key, v, expireSeconds);
		cacher2.get(key);//使用1次
		
		assertEquals(1, cacher2.usedTimes(key));//1
		assertEquals(1, cacher.usedTimes(key));//1
	}
	
	@Test
	public void testCRUD() throws Exception {
		MetricsCacher metricsCacher = new KeySizeMetricsCacher(L1);

		assertNull(metricsCacher.get(key));
		
		metricsCacher.set(key, v, expireSeconds);
		assertEquals(v, metricsCacher.get(key));
		
		metricsCacher.set(Arrays.asList(Tuples.of(key2, v2, expireSeconds)));
		assertEquals(v2, metricsCacher.get(key2));
		
		metricsCacher.remove(key);
		assertNull(metricsCacher.get(key));
		
		metricsCacher.remove(Arrays.asList(key2));
		assertNull(metricsCacher.get(key2));
	}
	
	@Test
	public void testUsedTimes() throws Exception {
		MetricsCacher metricsCacher = new KeySizeMetricsCacher(L1);
		
		assertEquals(0, metricsCacher.usedTimes(key));//没有key
		
		metricsCacher.set(key, v, expireSeconds);
		assertEquals(0, metricsCacher.usedTimes(key));//有key没有使用过
		
		metricsCacher.get(key);//使用1次
		assertEquals(1, metricsCacher.usedTimes(key));
	}

	@Test
	public void testexpireSeconds() throws Exception {
		MetricsCacher metricsCacher = new KeySizeMetricsCacher(L1);
		
		metricsCacher.set(key, v, expireSeconds);
		
		assertEquals(expireSeconds, metricsCacher.expireSeconds(key));
	}
	
	@Test
	public void testuseTimesAvg() throws Exception {
		MetricsCacher metricsCacher = new KeySizeMetricsCacher(L1);
		
		metricsCacher.set(key, v, expireSeconds);
		assertEquals(0, metricsCacher.usedTimesAvg());
		
		metricsCacher.get(key);
		Thread.sleep(2000);
		assertEquals(1, metricsCacher.usedTimesAvg());
		
		metricsCacher.set(key2, v2, expireSeconds);
		
		metricsCacher.get(key2);
		assertEquals(1, metricsCacher.usedTimesAvg());
	}
	
	@Test
	public void testremainExpireSeconds() throws Exception {
		MetricsCacher metricsCacher = new KeySizeMetricsCacher(L1);
		metricsCacher.set(key, v, expireSeconds);
		int remainExpireSeconds = metricsCacher.remainExpireSeconds(key);
		assertTrue(remainExpireSeconds > 0);
		assertTrue(remainExpireSeconds <= expireSeconds);
		assertTrue(remainExpireSeconds >= expireSeconds - 1);//比刚刚过去的1s要大
	}
}
