package io.github.icodegarden.wing.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.KeySizeLRUCacher;
import io.github.icodegarden.wing.common.ArgumentException;
import io.github.icodegarden.wing.java.DefaultDirectMemoryCacher;
import io.github.icodegarden.wing.java.DirectMemoryCacher;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.level.GetOfUpgradeStrategy;
import io.github.icodegarden.wing.level.Level;
import io.github.icodegarden.wing.level.LevelableCacher;
import io.github.icodegarden.wing.level.OutOfLimitStrategy;
import io.github.icodegarden.wing.level.SetOfFromStrategy;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@ExtendWith(MockitoJUnitRunner.class)
public class LevelableCacherTests {

	KeySizeMetricsCacher L1 = new KeySizeMetricsCacher( new HeapMemoryCacher());
	KeySizeMetricsCacher L2 = new KeySizeMetricsCacher( new DefaultDirectMemoryCacher());
	
	String key = "key";
	String v = "v";
	String key2 = "key2";
	String v2 = "v2";
	int seconds = 10;
	
	GetOfUpgradeStrategy getOfUpgradeStrategy = mock(GetOfUpgradeStrategy.class);
	
	LevelableCacher levelableCacher;
	
	@BeforeEach
	public void before() {
		levelableCacher = new LevelableCacher(
				Arrays.asList(new KeySizeLRUCacher(L1,1), new KeySizeLRUCacher(L2,1)),
				new SetOfFromStrategy.Lowest(), new OutOfLimitStrategy.ToNextLevel(),getOfUpgradeStrategy);
	}
	
	@Test
	public void testnumberOfLevels() throws Exception {
		assertEquals(2, levelableCacher.numberOfLevels());
	}
	
	@Test
	public void testgetLevel() throws Exception {
		assertTrue(levelableCacher.getLevel(1).getCacher().instanceOf(HeapMemoryCacher.class));
		assertTrue(levelableCacher.getLevel(2).getCacher().instanceOf(DirectMemoryCacher.class));
		try {
			levelableCacher.getLevel(3);
			throw new RuntimeException("到这里失败");
		} catch (IndexOutOfBoundsException e) {
		}
	}
	
	/**
	 * 从L1获取到，L2没有
	 */
	@Test
	public void testGet1() throws Exception {
		levelableCacher.set(key, v, seconds);
		
		Object object = levelableCacher.get(key);
		
		assertEquals(v, object);
		
		assertNull(L2.get(key));
		
		verify(getOfUpgradeStrategy, times(1)).upgrade(any(Level.class), eq(key), eq(v), any(OutOfLimitStrategy.class));
	}
	/**
	 * 从L1获取不到，从L2获取到
	 */
	@Test
	public void testGet2() throws Exception {
		levelableCacher.set(key, v, seconds);
		levelableCacher.set(key2, v2, seconds);
		
		Object object = levelableCacher.get(key);
		assertEquals(v, object);
		
		assertEquals(v2, L1.get(key2));//v2在L1
		assertEquals(v, L2.get(key));//v在L2
		
		verify(getOfUpgradeStrategy, times(1)).upgrade(any(Level.class), eq(key), eq(v), any(OutOfLimitStrategy.class));
	}
	
	@Test
	public void testGetBatch() throws Exception {
		levelableCacher.set(key, v, seconds);
		levelableCacher.set(key2, v2, seconds);
		
		List<String> keys = Arrays.asList(key,key2);
		Map<String, Object> map = levelableCacher.get(keys);
		assertEquals(v, map.get(key));
		assertEquals(v2, map.get(key2));
		
		verify(getOfUpgradeStrategy, times(1)).upgrade(any(Level.class), eq(key), eq(v), any(OutOfLimitStrategy.class));
		verify(getOfUpgradeStrategy, times(1)).upgrade(any(Level.class), eq(key2), eq(v2), any(OutOfLimitStrategy.class));
		
		
		Cacher L2 = mock(Cacher.class);
		levelableCacher = new LevelableCacher(
				Arrays.asList(new KeySizeLRUCacher(L1,1), L2),
				new SetOfFromStrategy.Lowest(), new OutOfLimitStrategy.ToNextLevel(),getOfUpgradeStrategy);
		levelableCacher.remove(keys);//清空
		
		map = levelableCacher.get(keys);
		assertTrue(map.keySet().containsAll(keys));
		verify(L2, times(1)).get(keys);//由于L1中没有，会从L2去获取
	}
	
	@Test
	public void testSetBatch() throws Exception {
		try{
			levelableCacher.set(Arrays.asList(Tuples.of(key, v, seconds),Tuples.of(key2, v2, seconds)));
			throw new RuntimeException();//跑到这里是失败
		}catch (ArgumentException e) {
			assertEquals("maxKeySize:1 <(lt) request key size:2", e.getMessage());
		}
		
		Object object = levelableCacher.get(key);
		assertNull(object);//没有进缓存
	}
	
	@Test
	public void testRemove() throws Exception {
		levelableCacher.set(key, v, seconds);
		Object object = levelableCacher.get(key);
		assertEquals(v, object);
		
		levelableCacher.remove(key);
		assertNull(levelableCacher.get(key));
	}
	
	@Test
	public void testRemoves() throws Exception {
		levelableCacher.set(key, v, seconds);
		levelableCacher.set(key2, v2, seconds);
		assertEquals(v, levelableCacher.get(key));
		assertEquals(v2, levelableCacher.get(key2));
		
		levelableCacher.remove(Arrays.asList(key,key2));
		assertNull(levelableCacher.get(key));
		assertNull(levelableCacher.get(key2));
	}
}
