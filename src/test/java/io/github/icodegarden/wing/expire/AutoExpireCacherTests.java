package io.github.icodegarden.wing.expire;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.UserForTests;
import io.github.icodegarden.wing.expire.AutoExpireCacher;
import io.github.icodegarden.wing.java.HeapMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class AutoExpireCacherTests {

	private String key = "key";
	private UserForTests v = new UserForTests("name", 18);
	private String key2 = "key2";
	private UserForTests v2 = new UserForTests("name2", 19);
	private int expireSeconds = 1;
	
	HeapMemoryCacher heapMemoryCacher = new HeapMemoryCacher();
	

	@BeforeEach
	public void before() {
		
	}
	
	@Test
	public void testDefaultScan() throws Exception {
		AutoExpireCacher cacher = new AutoExpireCacher(heapMemoryCacher);
		cacher.set(key, v, expireSeconds);
		
		Thread.sleep(expireSeconds*1000 - 10);//比到期早一点
		assertNotNull(cacher.get(key));//scan 10s
		
		Thread.sleep(11*1000);
		assertNull(cacher.get(key));
	}
	
	@Test
	public void testDefaultScanStandalone() throws Exception {
		AutoExpireCacher cacher = new AutoExpireCacher(heapMemoryCacher, true);
		cacher.set(key, v, expireSeconds);
		
		Thread.sleep(expireSeconds*1000);
		assertNotNull(cacher.get(key));//scan 10s
		
		Thread.sleep(11*1000);
		assertNull(cacher.get(key));
	}
	
	@Test
	public void testAssignedScan() throws Exception {
		AutoExpireCacher cacher = new AutoExpireCacher(heapMemoryCacher, 1);
		cacher.set(key, v, expireSeconds);
		
		Thread.sleep(10000);
		assertNull(cacher.get(key));
	}
	
	@Test
	public void testAssignedScanStandalone() throws Exception {
		AutoExpireCacher cacher = new AutoExpireCacher(heapMemoryCacher, 1,true);
		cacher.set(key, v, expireSeconds);
		
		Thread.sleep(10000);
		assertNull(cacher.get(key));
	}
}
