package org.supermoto.yamaha.R1.io.cache.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.SpaceCalcableCacher;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import org.supermoto.yamaha.R1.io.cache.java.DefaultDirectMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.metrics.SpaceMetricsCacher;

import io.cache.UserForTests;


/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class SpaceMetricsCacherTests {

	String key = "key";
	UserForTests v = new UserForTests("name", 18);
	String key2 = "key2";
	UserForTests v2 = new UserForTests("name2", 28);
	int expireSeconds = 10;
	
	SpaceCalcableCacher spaceCalcableCacher = new DefaultDirectMemoryCacher();
	SpaceMetricsCacher spaceMetricsCacher = new SpaceMetricsCacher(spaceCalcableCacher);
	
	@Test
	public void testusedSpaceSize() throws Exception {
		spaceMetricsCacher.set(key, v, expireSeconds);
		
		assertEquals(spaceCalcableCacher.spaceSizeCalc(v), spaceMetricsCacher.usedSpaceSize());
		
		spaceMetricsCacher.set(key, v2, expireSeconds);//v2替换v
		
		assertEquals(spaceCalcableCacher.spaceSizeCalc(v2), spaceMetricsCacher.usedSpaceSize());
		
		spaceMetricsCacher.remove(key);//清0
		assertEquals(0, spaceMetricsCacher.usedSpaceSize());
		
		spaceMetricsCacher.set(Arrays.asList(Tuples.of(key, v, expireSeconds),Tuples.of(key2, v2, expireSeconds)));//重新设置
		
		assertEquals((long)spaceCalcableCacher.spaceSizeCalc(v) + (long)spaceCalcableCacher.spaceSizeCalc(v2), spaceMetricsCacher.usedSpaceSize());
	}
	
}
