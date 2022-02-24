package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.common.RejectedRequestException;
import io.github.icodegarden.wing.protect.BlackListFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class BlackListFilterTests {
	
	protected String key = "key";
	protected String key2 = "key2";
	
	@Test
	public void testNotShouldFilter() throws Exception {
		BlackListFilter filter = new BlackListFilter(Arrays.asList("key"),k->false);
		filter.doFilter(key);
	}
	
	@Test
	public void testDoFilter() throws Exception {
		BlackListFilter filter = new BlackListFilter(Arrays.asList("key"),k->true);
		
		filter.doFilter(key2);
		try{
			filter.doFilter(key);
			throw new AssertionError("期望异常");
		}catch (RejectedRequestException e) {
			assertEquals(filter, e.getRejector());
			assertEquals("request key:" + key + " reject by black list", e.getReason());
		}
	}
}
