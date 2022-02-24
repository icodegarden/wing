package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.UserForTests;
import io.github.icodegarden.wing.protect.Protector;
import io.github.icodegarden.wing.protect.ProtectorChain;
import io.github.icodegarden.wing.protect.ProtectorChain.Default;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class ProtectorTests {
	
	protected Protector protector;
	
	@BeforeEach
	public void loadProtector() {
		protector = getProtector();
	}
	
	protected abstract Protector getProtector();

	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected String key2 = "key2";
	protected UserForTests v2 = new UserForTests("name2", 19);
	protected int expireSeconds = 10;

	Cacher cacher = spy(Cacher.class);

	@Test
	public void testDoProtector() throws Exception {
		long sleep = 1000;

		new Thread() {
			public void run() {
				Default<UserForTests> chain = new ProtectorChain.Default<UserForTests>(cacher, key, () -> {
					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
					}
					return v;
				}, expireSeconds, Arrays.asList(protector));
				chain.doProtector();
			}
		}.start();

		long sleep2 = 10;
		Thread.sleep(sleep2);

		long start = System.currentTimeMillis();
		
		Default<UserForTests> chain = new ProtectorChain.Default<UserForTests>(cacher, key, () -> v, expireSeconds, Arrays.asList(protector));
		
		UserForTests user = chain.doProtector();
		
		long end = System.currentTimeMillis();

		assertEquals(v, user);
		assertTrue((end - start) >= (sleep - sleep2));
	}
}
