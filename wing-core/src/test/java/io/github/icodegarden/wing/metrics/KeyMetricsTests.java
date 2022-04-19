package io.github.icodegarden.wing.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.metrics.KeyMetrics;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KeyMetricsTests {

	String key = "key";
	String v = "v";
	int expireSeconds = 1;
	String key2 = "key2";
	String v2 = "v2";
	
	KeyMetrics keyMetrics = new KeyMetrics();
	
	@BeforeEach
	public void before() {
		keyMetrics.set(key, expireSeconds);
		keyMetrics.set(key2, expireSeconds);
	}
	
	@Test
	public void testKeys() throws Exception {
		assertTrue(keyMetrics.keys().containsAll(Arrays.asList(key,key2)));
	}
	
	@Test
	public void testKeySize() throws Exception {
		assertEquals(2, keyMetrics.keySize());
	}
	
	@Test
	public void testincrementUsedTimes() throws Exception {
		assertEquals(0, keyMetrics.usedTimes(key));//从未使用过
		
		keyMetrics.incrementUsedTimes(key);//增加1次
		
		assertEquals(1, keyMetrics.usedTimes(key));
		
		keyMetrics.incrementUsedTimes("key3");//会给不存在的key增加
		assertEquals(1, keyMetrics.usedTimes("key3"));
	}
	@Test
	public void testusedTimesAvg() throws Exception {
		keyMetrics.incrementUsedTimes(key);//增加1次
		keyMetrics.incrementUsedTimes(key2);//增加1次
		Thread.sleep(5000);
		assertEquals(1, keyMetrics.usedTimesAvg());
		
		keyMetrics.incrementUsedTimes(key);//增加1次
		keyMetrics.incrementUsedTimes(key);//增加1次
		keyMetrics.incrementUsedTimes(key2);//增加1次
		keyMetrics.incrementUsedTimes(key2);//增加1次
		Thread.sleep(5000);
		assertEquals(3, keyMetrics.usedTimesAvg());
	}
	
	@Test
	public void testresetUsedTimes() throws Exception {
		testusedTimesAvg();
		keyMetrics.resetUsedTimes();
		testKeys();//重置后key还在
		
		assertEquals(0, keyMetrics.usedTimes(key));
		assertEquals(0, keyMetrics.usedTimes(key2));
	}
	
	@Test
	public void testkeysUsedTimesLte() throws Exception {
		keyMetrics.incrementUsedTimes(key);//增加1次
		keyMetrics.incrementUsedTimes(key);//增加1次
		
		Collection<String> keysUsedTimesLte = keyMetrics.keysUsedTimesLte(1);//key2符合
		assertEquals(1, keysUsedTimesLte.size());
		assertEquals(key2, keysUsedTimesLte.iterator().next());
		
		keysUsedTimesLte = keyMetrics.keysUsedTimesLte(2);//key key2都符合
		assertEquals(2, keysUsedTimesLte.size());
	}
	
	@Test
	public void testexpireSeconds() throws Exception {
		assertEquals(expireSeconds, keyMetrics.expireSeconds(key));
		assertEquals(expireSeconds, keyMetrics.expireSeconds(key2));
	}
	
	@Test
	public void testexpiredKeys() throws Exception {
		assertEquals(0, keyMetrics.expiredKeys().size());
		Thread.sleep(expireSeconds * 1000 + 10000);//多10s
		assertEquals(2, keyMetrics.expiredKeys().size());
	}
}
