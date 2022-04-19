package io.github.icodegarden.wing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.wing.KeySizeLRUCacher;
import io.github.icodegarden.wing.SpaceSizeLRUCacher;
import io.github.icodegarden.wing.common.ArgumentException;
import io.github.icodegarden.wing.java.DefaultDirectMemoryCacher;
import io.github.icodegarden.wing.java.DirectMemoryCacher;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;
import io.github.icodegarden.wing.metrics.SpaceMetricsCacher;


/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KeySizeLRUCacherTests {
	
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
		KeySizeMetricsCacher keySizeMetricsCacher = new KeySizeMetricsCacher(new HeapMemoryCacher());
		KeySizeLRUCacher cacher = new KeySizeLRUCacher(keySizeMetricsCacher,1);
		
		try{
			cacher.set(Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds)));
			throw new RuntimeException("expect IllegalArgumentException");
		}catch (ArgumentException e) {
			assertEquals("maxKeySize:1 <(lt) request key size:2", e.getMessage());
		}
	}
	
	/**
	 * 设置最多1个容量，第一次set不会触发移除，第二次触发移除
	 */
	@Test
	public void testLRU() throws Exception {
		KeySizeMetricsCacher keySizeMetricsCacher = new KeySizeMetricsCacher(new HeapMemoryCacher());
		KeySizeLRUCacher cacher = new KeySizeLRUCacher(keySizeMetricsCacher,1);

		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);// 不触发溢出
		assertNull(removes);

		removes = cacher.set(key2, v2, expireSeconds);// 触发溢出
		assertEquals(1, removes.size());
		assertEquals(v, removes.get(0).getT2());

		assertEquals(v2, cacher.get(key2));// 里面存在的是v2
	}
	
	/**
	 * 嵌套1层，最多1个容量，第一次set不会触发移除，第二次触发移除
	 */
	@Test
	public void testMulti() throws Exception {
		DirectMemoryCacher directMemoryCacher = new DefaultDirectMemoryCacher();
		SpaceMetricsCacher spaceMetricsCacher = new SpaceMetricsCacher(directMemoryCacher);
		SpaceSizeLRUCacher spaceSizeLRUCacher = new SpaceSizeLRUCacher(spaceMetricsCacher, directMemoryCacher.spaceSizeCalc(v));// 设置一个v的大小
		
		KeySizeMetricsCacher keySizeMetricsCacher = new KeySizeMetricsCacher(spaceSizeLRUCacher);
		KeySizeLRUCacher cacher = new KeySizeLRUCacher(keySizeMetricsCacher,1);

		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);// 不触发溢出
		assertNull(removes);

		removes = cacher.set(key2, v2, expireSeconds);// 触发溢出
		assertEquals(1, removes.size());
		assertEquals(v, removes.get(0).getT2());

		assertEquals(v2, cacher.get(key2));// 里面存在的是v2
	}
	
}
