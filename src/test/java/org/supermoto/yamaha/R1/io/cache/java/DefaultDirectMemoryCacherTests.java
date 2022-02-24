package org.supermoto.yamaha.R1.io.cache.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.CacherTests;
import org.supermoto.yamaha.R1.io.cache.java.DefaultDirectMemoryCacher;
import org.supermoto.yamaha.R1.io.cache.java.DirectMemoryCacher;

import io.cache.UserForTests;

/**
 * -XX:MaxDirectMemorySize=50M -Xms256m -Xmx256m -Xss256K -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class DefaultDirectMemoryCacherTests extends CacherTests {
	
	private static Logger log = LoggerFactory.getLogger(DefaultDirectMemoryCacherTests.class);

	DefaultDirectMemoryCacher directMemoryCacher;

	String key = "key";
	UserForTests v = new UserForTests("name", 18);
	String key2 = "key2";
	UserForTests v2 = new UserForTests("name2", 19);
	String key3 = "key3";
	UserForTests v3 = new UserForTests("name33", 20);
	String key4 = "key4";
	UserForTests v4 = new UserForTests("name444", 21);
	int expireSeconds = 10;

	@BeforeEach
	public void before() {
	}
	
	@Override
	protected Cacher getCacher() {
		if(directMemoryCacher != null) {
			return directMemoryCacher;
		}
		directMemoryCacher = new DefaultDirectMemoryCacher();
		return directMemoryCacher;
	}
	
	/**
	 * 只加不删会溢出
	 * @throws Exception
	 */
	@Test
	public void testOOM() throws Exception {
		final byte[] _5MB = new byte[5 * 1024 * 1024];
		// 设置 -XX:MaxDirectMemorySize=?M
		try {
			for (int i = 0; i < 512; i++) {
				getCacher().set(i + "", _5MB, expireSeconds);
			}
			throw new RuntimeException("expect OOM");
		} catch (OutOfMemoryError e) {
			assertTrue(e.getMessage().contains("Direct buffer memory") || e.getMessage().contains("Cannot reserve"));
			
			clearCache();//由于testOOM，内部塞满了，会导致testNoOOM内存不够
		}
	}

	/**
	 * 加了再删不会溢出
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNoOOM() throws Exception {
		final byte[] _5MB = new byte[5 * 1024 * 1024];
		// 设置 -XX:MaxDirectMemorySize=?M
		for (int i = 0; i < 512; i++) {
			getCacher().set(i + "", _5MB, expireSeconds);
			getCacher().remove(i + "");
			log.debug("testNoOOM loop:" + i);
		}
	}

	@Test
	public void testspaceSizeCalc() throws Exception {
		int size = ((DirectMemoryCacher)getCacher()).spaceSizeCalc(v);
		assertTrue(size > 0);
	}
}
