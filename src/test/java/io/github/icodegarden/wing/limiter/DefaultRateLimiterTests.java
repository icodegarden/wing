package io.github.icodegarden.wing.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.limiter.DefaultRateLimiter;
import io.github.icodegarden.wing.limiter.Dimension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class DefaultRateLimiterTests {
	
	@Test
	public void testisAllowable() throws Exception {
		DefaultRateLimiter defaultTpsLimiter = new DefaultRateLimiter(2);
		
		Dimension dimension = new Dimension("name", 2, 1000);
		
		assertEquals(true, defaultTpsLimiter.isAllowable(dimension));
		assertEquals(true, defaultTpsLimiter.isAllowable(dimension));
		assertEquals(false, defaultTpsLimiter.isAllowable(dimension));
		
		Thread.sleep(1100);
		assertEquals(true, defaultTpsLimiter.isAllowable(dimension));
		assertEquals(true, defaultTpsLimiter.isAllowable(dimension));
		assertEquals(false, defaultTpsLimiter.isAllowable(dimension));
	}
	
	@Test
	public void testisclearIfRequired() throws Exception {
		DefaultRateLimiter limiter = new DefaultRateLimiter(1);
		
		Dimension dimension = new Dimension("name", 2, 1000);
		
		assertEquals(true, limiter.isAllowable(dimension));
		
		Thread.sleep(10);
		
		limiter.clearIfRequired(1);
		assertTrue(limiter.getDimensions().containsKey("name"));
		
		Thread.sleep(1000);
		
		dimension = new Dimension("name2", 2, 1000);
		limiter.isAllowable(dimension);
		
		limiter.clearIfRequired(1000);
		assertEquals(1,limiter.getDimensions().size());
		assertTrue(limiter.getDimensions().containsKey("name2"));
		
	}
}
