package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;
import org.supermoto.yamaha.R1.io.cache.limiter.DefaultRateLimiter;
import org.supermoto.yamaha.R1.io.cache.limiter.Dimension;
import org.supermoto.yamaha.R1.io.cache.protect.ProtectorChain;
import org.supermoto.yamaha.R1.io.cache.protect.RateLimitProtector;
import org.supermoto.yamaha.R1.io.cache.protect.ProtectorChain.Default;

import io.cache.UserForTests;

import static org.mockito.Mockito.*;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RateLimitProtectorTests {

	RateLimitProtector protector = new RateLimitProtector(new DefaultRateLimiter(),
			key -> new Dimension[] { new Dimension("test", 1, 1000) });

	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected String key2 = "key2";
	protected UserForTests v2 = new UserForTests("name2", 19);
	protected int expireSeconds = 10;

	Cacher cacher = spy(Cacher.class);

	@Test
	public void testDoProtector() throws Exception {
		Default<UserForTests> chain = new ProtectorChain.Default<UserForTests>(cacher, key, () -> v, expireSeconds, Arrays.asList(protector));
		
		UserForTests userForTests = chain.doProtector();
		
		assertEquals(v, userForTests);
		try {
			chain = new ProtectorChain.Default<UserForTests>(cacher, key, () -> v, expireSeconds, Arrays.asList(protector));
			
			chain.doProtector();
			throw new RuntimeException("到这里失败");
		} catch (RejectedRequestException e) {
			assertEquals(protector, e.getRejector());
			assertEquals("Rate Limited on load data when cache not found, key:" + key, e.getReason());
		}
	}
}
