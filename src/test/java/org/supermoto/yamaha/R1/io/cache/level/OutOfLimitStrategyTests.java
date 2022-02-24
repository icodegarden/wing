package org.supermoto.yamaha.R1.io.cache.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import org.supermoto.yamaha.R1.io.cache.java.DefaultDirectMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.java.HeapMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.level.Level;
import org.supermoto.yamaha.R1.io.cache.level.OutOfLimitStrategy;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class OutOfLimitStrategyTests {

	Cacher L1 = new HeapMemoryCacher();
	Cacher L2 = new DefaultDirectMemoryCacher();
	
	Level level = Level.of(Arrays.asList(L1,L2));
	
	String key = "key";
	String v = "v";
	String key2 = "key2";
	String v2 = "v2";
	String key3 = "key3";
	String v3 = "v3";
	int expireSeconds = 10;
	
	@BeforeEach
	public void before() {
	}
	
	@Test
	public void testToNextLevel() throws Exception {
		OutOfLimitStrategy outOfLimitStrategy = new OutOfLimitStrategy.ToNextLevel();
		List<Tuple3<String, Object, Integer>> removes = Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds));
		List<Tuple3<String, Object, Integer>> drops = outOfLimitStrategy.set(level, removes);
		
		assertNull(drops);//没有丢弃
		
		assertEquals(v, L2.get(key));//到了L2
		assertEquals(v2, L2.get(key2));//到了L2
	}
	
	@Test
	public void testDrop() throws Exception {
		OutOfLimitStrategy outOfLimitStrategy = new OutOfLimitStrategy.Drop();
		List<Tuple3<String, Object, Integer>> removes = Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds));
		List<Tuple3<String, Object, Integer>> drops = outOfLimitStrategy.set(level, removes);
		
		assertEquals(removes, drops);//全部丢弃
		
		assertNull(L2.get(key));//没到L2
		assertNull(L2.get(key2));//没到L2
	}
}
