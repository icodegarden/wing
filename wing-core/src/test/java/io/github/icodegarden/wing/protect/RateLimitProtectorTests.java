package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.limiter.CounterRateLimiter;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.UserForTests;
import io.github.icodegarden.wing.common.RejectedCacheException;
import io.github.icodegarden.wing.protect.ProtectorChain.Default;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class RateLimitProtectorTests {

	RateLimitProtector protector = new RateLimitProtector(new CounterRateLimiter(1, 1000));

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
		} catch (RejectedCacheException e) {
			assertEquals(protector, e.getRejector());
			assertEquals("Rate Limited on load data when cache not found, key:" + key, e.getReason());
		}
	}
}
