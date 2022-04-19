package io.github.icodegarden.wing.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.level.Level;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class LevelTests {

	@Mock
	Cacher L1;
	@Mock
	Cacher L2;
	@Mock
	Cacher L3;
	
	@Test
	public void testOf() throws Exception {
		Level level = Level.of(Arrays.asList(L1,L2,L3));
		
		assertEquals(L1, level.getCacher());
		assertEquals(L2, level.getNext().getCacher());
		assertEquals(L3, level.getNext().getNext().getCacher());
		
		assertNull(level.getPre());
		assertEquals(L1, level.getNext().getPre().getCacher());
		assertEquals(L2, level.getNext().getNext().getPre().getCacher());
	}

}
