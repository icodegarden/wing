package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;
import org.supermoto.yamaha.R1.io.cache.protect.RegexFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RegexFilterTests {
	
	protected String key = "key";
	protected String key2 = "key2";
	
	@Test
	public void testNotShouldFilter() throws Exception {
		RegexFilter regexFilter = new RegexFilter("\\d", k->false);//只放行数字，但should=false
		regexFilter.doFilter(key);
	}
	
	@Test
	public void testDoFilter() throws Exception {
		String regex = "\\d";
		RegexFilter regexFilter = new RegexFilter(regex, k->true);
		
		regexFilter.doFilter("1");
		try{
			regexFilter.doFilter(key2);
			throw new AssertionError("期望异常");
		}catch (RejectedRequestException e) {
			assertEquals(regexFilter, e.getRejector());
			assertEquals("request key:" + key2 + " reject by regex:" + regex, e.getReason());
		}
	}
}
