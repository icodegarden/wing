package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class ProtectorChainTests {

	RateLimitProtector rateLimitProtector = new RateLimitProtector(new CounterRateLimiter(1, 200));
	SynchronizedDoubleCheckProtector synchronizedDoubleCheckProtector = new SynchronizedDoubleCheckProtector();

	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected String key2 = "key2";
	protected UserForTests v2 = new UserForTests("name2", 19);
	protected int expireSeconds = 10;

	Cacher cacher = spy(Cacher.class);

	@Test
	public void testDoProtector() throws Exception {
		//第一次 
		long sleep = 1000;
		new Thread() {
			public void run() {
				Default<UserForTests> chain = buildChain(sleep);
				chain.doProtector();
			}
		}.start();

		long sleep2 = 100;
		Thread.sleep(sleep2);
		
		//第二次 被RateLimitProtector限流
		try{
			Default<UserForTests> chain = buildChain(sleep);
			chain.doProtector();
			throw new RuntimeException("到这里失败");
		} catch (RejectedCacheException e) {
			assertEquals(rateLimitProtector, e.getRejector());
		}
		
		Thread.sleep(sleep2+=200);//不休息会被限流
		
		//第三次 被SynchronizedDoubleCheckProtector同步执行
		long start = System.currentTimeMillis();
		
		Default<UserForTests> chain = buildChain(0);
		UserForTests user = chain.doProtector();
		
		long end = System.currentTimeMillis();
		assertEquals(v, user);
		
		assertTrue((end - start) >= 500);
	}

	private Default<UserForTests> buildChain(long fromSupplierSleep) {
		Default<UserForTests> chain = new ProtectorChain.Default<UserForTests>(cacher, key, () -> {
			try {
				Thread.sleep(fromSupplierSleep);
			} catch (InterruptedException e) {
			}
			return v;
		}, expireSeconds, Arrays.asList(rateLimitProtector,synchronizedDoubleCheckProtector));
		return chain;
	}
	
	
}
