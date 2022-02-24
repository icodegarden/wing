package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;
import org.supermoto.yamaha.R1.io.cache.limiter.DefaultRateLimiter;
import org.supermoto.yamaha.R1.io.cache.limiter.Dimension;
import org.supermoto.yamaha.R1.io.cache.protect.ProtectorChain;
import org.supermoto.yamaha.R1.io.cache.protect.RateLimitProtector;
import org.supermoto.yamaha.R1.io.cache.protect.SynchronizedDoubleCheckProtector;
import org.supermoto.yamaha.R1.io.cache.protect.ProtectorChain.Default;

import io.cache.UserForTests;

import static org.mockito.Mockito.*;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class ProtectorChainTests {

	RateLimitProtector rateLimitProtector = new RateLimitProtector(new DefaultRateLimiter(),
			key -> new Dimension[] { new Dimension("test", 1, 200) });
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
		} catch (RejectedRequestException e) {
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
