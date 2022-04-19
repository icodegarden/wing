package io.github.icodegarden.wing.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.limiter.Dimension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class DimensionTests {
	
	@Test
	public void testisAllowable() throws Exception {
		Dimension dimension = new Dimension("name", 2, 1000);
		
		assertEquals(true, dimension.isAllowable());
		assertEquals(true, dimension.isAllowable());
		assertEquals(false, dimension.isAllowable());
		
		Thread.sleep(1100);
		assertEquals(true, dimension.isAllowable());
		assertEquals(true, dimension.isAllowable());
		assertEquals(false, dimension.isAllowable());
	}
}
