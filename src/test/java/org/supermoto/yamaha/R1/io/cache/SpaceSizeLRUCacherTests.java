package org.supermoto.yamaha.R1.io.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.SpaceSizeLRUCacher;
import org.supermoto.yamaha.R1.io.cache.common.ArgumentException;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import org.supermoto.yamaha.R1.io.cache.java.DefaultDirectMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.java.DirectMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.metrics.SpaceMetricsCacher;

import io.cache.UserForTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class SpaceSizeLRUCacherTests {
	DirectMemoryCacher directMemoryCacher = new DefaultDirectMemoryCacher();
	SpaceMetricsCacher spaceMetricsCacher = new SpaceMetricsCacher(directMemoryCacher);

	String key = "key";
	UserForTests v = new UserForTests("name", 18);
	String key2 = "key2";
	UserForTests v2 = new UserForTests("namy", 19);
	int expireSeconds = 10;

	/**
	 * 超过限制
	 * @throws Exception
	 */
	@Test
	public void testOutOfLimit() throws Exception {
		SpaceSizeLRUCacher cacher = new SpaceSizeLRUCacher(spaceMetricsCacher, directMemoryCacher.spaceSizeCalc(v));// 设置一个v的大小
		try{
			cacher.set(Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds)));
			throw new RuntimeException("expect IllegalArgumentException");
		}catch (ArgumentException e) {
			assertEquals("maxBytes:87 <(lt) request key needSpace:174", e.getMessage());
		}
	}
	
	/**
	 * 设置最多1个容量，第一次set不会触发移除，第二次触发移除
	 */
	@Test
	public void testLRU() throws Exception {
		SpaceSizeLRUCacher cacher = new SpaceSizeLRUCacher(spaceMetricsCacher, directMemoryCacher.spaceSizeCalc(v));// 设置一个v的大小

		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);// 不触发溢出
		assertNull(removes);
		
		removes = cacher.set(key2, v2, expireSeconds);// 触发溢出
		assertEquals(1, removes.size());
		assertEquals(v, removes.get(0).getT2());
		assertEquals(v2, cacher.get(key2));// 里面存在的是v2
	}
	
	@Test
	public void testusedSpaceSize() throws Exception {
		SpaceSizeLRUCacher cacher = new SpaceSizeLRUCacher(spaceMetricsCacher, 1024);// 设置足够
		cacher.set(key, v, expireSeconds);
		
		cacher.set(key, v2, expireSeconds);//v2替换v
		assertEquals(directMemoryCacher.spaceSizeCalc(v2), spaceMetricsCacher.usedSpaceSize());
		
		spaceMetricsCacher.remove(key);//清0
		assertEquals(0, spaceMetricsCacher.usedSpaceSize());
		
		cacher.set(Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds)));//重新设置
		
		assertEquals((long)directMemoryCacher.spaceSizeCalc(v) + (long)directMemoryCacher.spaceSizeCalc(v2), spaceMetricsCacher.usedSpaceSize());
	}
}
