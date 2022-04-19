package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.common.RejectedRequestException;
import io.github.icodegarden.wing.protect.WhiteListFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class WhiteListFilterTests {
	
	protected String key = "key";
	protected String key2 = "key2";
	
	@Test
	public void testNotShouldFilter() throws Exception {
		WhiteListFilter filter = new WhiteListFilter(Arrays.asList("key"),k->false);
		filter.doFilter(key2);
	}
	
	@Test
	public void testDoFilter() throws Exception {
		WhiteListFilter filter = new WhiteListFilter(Arrays.asList("key"),k->true);
		
		filter.doFilter(key);
		try{
			filter.doFilter(key2);
			throw new AssertionError("期望异常");
		}catch (RejectedRequestException e) {
			assertEquals(filter, e.getRejector());
			assertEquals("request key:" + key2 + " reject by white list", e.getReason());
		}
	}
}
