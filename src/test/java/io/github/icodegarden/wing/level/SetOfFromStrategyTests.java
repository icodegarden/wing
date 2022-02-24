package io.github.icodegarden.wing.level;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.level.Level;
import io.github.icodegarden.wing.level.SetOfFromStrategy;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class SetOfFromStrategyTests {

	@Mock
	Cacher L1;
	@Mock
	Cacher L2;
	@Mock
	Cacher L3;
	
	Level level;
	
	String key = "key";
	String v = "v";
	int seconds = 10;
	
	@BeforeEach
	public void before() {
		level = Level.of(Arrays.asList(L1,L2,L3));
	}
	
	@Test
	public void testLowest() throws Exception {
		SetOfFromStrategy strategy = new SetOfFromStrategy.Lowest();
//		strategy.set(level, (level)->{
//			level.getCacher().set(key, v, seconds);
//			return null;
//		});
		
		Level select = strategy.select(level);
		assertEquals(L1, select.getCacher());
		
//		verify(L1,times(1)).set(key, v, seconds);
//		verify(L2,times(0)).set(key, v, seconds);
	}

	@Test
	public void testHighest() throws Exception {
		SetOfFromStrategy strategy = new SetOfFromStrategy.Highest();
//		strategy.set(level, (level)->{
//			level.getCacher().set(key, v, seconds);
//			return null;
//		});
		
		Level select = strategy.select(level);
		assertEquals(L3, select.getCacher());
		
//		verify(L1,times(0)).set(key, v, seconds);
//		verify(L2,times(1)).set(key, v, seconds);
	}
}
