package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;
import org.supermoto.yamaha.R1.io.cache.protect.BloomFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class BloomFilterTests {
	
	protected String key = "key";
	protected String key2 = "key2";
	
	@Test
	public void testNotShouldFilter() throws Exception {
		BloomFilter filter = new BloomFilter(3, k->false);
		filter.doFilter(key);
	}
	
	@Test
	public void testDoFilter() throws Exception {
		BloomFilter filter = new BloomFilter(3, k->true);
		filter.add(key);
		
		filter.doFilter(key);
		try{
			filter.doFilter(key2);
			throw new AssertionError("期望异常");
		}catch (RejectedRequestException e) {
			assertEquals(filter, e.getRejector());
			assertEquals("request key:" + key2 + " reject by bloom filter", e.getReason());
		}

		filter.add(key2);
		filter.doFilter(key2);
	}
}
