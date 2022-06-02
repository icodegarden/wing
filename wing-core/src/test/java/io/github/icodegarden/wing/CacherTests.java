package io.github.icodegarden.wing;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;


/**
 * 
 * @author Fangfang.Xu
 *
 */
//@ExtendWith(MockitoJUnitRunner.class)
public abstract class CacherTests extends PerformanceTests {
	private static Logger log = LoggerFactory.getLogger(CacherTests.class);
	
	protected boolean notnullOnRemoveIfExists() {
		return true;
	}

	protected String key = "key";
	protected UserForTests v = new UserForTests("name", 18);
	protected String key2 = "key2";
	protected UserForTests v2 = new UserForTests("name2", 19);
	protected String key3 = "key3";
	protected UserForTests v3 = new UserForTests("name3", 20);
	protected int expireSeconds = 10;
	
	@AfterEach
	public void autoClearCache() throws Exception {
		try{
			clearCache();
		}catch (Exception e) {
		}
	}
	
	protected void clearCache() throws Exception {
		Field f = getCacher().getClass().getDeclaredField("map");
		f.setAccessible(true);
		Map map = (Map)f.get(getCacher());
		map.clear();
	}

	@Test
	public void testGet() throws Exception {
		testSet();

		assertEquals(v, cacher.get(key));
		assertEquals(v, cacher.get(key));// 再次读取
	}

	@Test
	public void testGetBatch() throws Exception {
		Map<String, Object> map = cacher.get(Arrays.asList(key, key2));
		assertNotNull(map);//约定不允许为null
		assertEquals(2, map.size());//key的size与请求数量一样
		assertEquals(0, map.values().stream().filter(i->i != null).collect(Collectors.toList()).size());
		
		cacher.set(key, v, expireSeconds);
		cacher.set(key2, v2, expireSeconds);

		map = cacher.get(Arrays.asList(key, key2));

		assertEquals(v, map.get(key));
		assertEquals(v2, map.get(key2));// 再次读取
	}

	@Test
	public void testgetElseSupplier() throws Exception {
		assertNull(cacher.get(key));

		assertEquals(v, cacher.getElseSupplier(key, () -> v, expireSeconds));
		assertEquals(v, cacher.getElseSupplier(key, () -> v2, expireSeconds));// supplier v2 但是返回v
	}

	@Test
	public void testgetElseSupplier2() throws Exception {
		Map<String, Object> map = cacher.get(Arrays.asList(key, key2));
		assertNull(map.get(key));
		assertNull(map.get(key2));

		map = cacher.getElseSupplier(
				Arrays.asList(Tuples.of(key, () -> v, expireSeconds), Tuples.of(key2, () -> v2, expireSeconds)));

		assertEquals(v, map.get(key));
		assertEquals(v2, map.get(key2));

		map = cacher.getElseSupplier(
				Arrays.asList(Tuples.of(key, () -> v2, expireSeconds), Tuples.of(key2, () -> v, expireSeconds)));

		assertEquals(v, map.get(key));// supplier v2 但是返回v
		assertEquals(v2, map.get(key2));// supplier v 但是返回v2
	}

	@Test
	public void testgetThenPredicateElseSupplier() throws Exception {
		Map<String, Object> map = cacher.get(Arrays.asList(key, key2));
		assertNull(map.get(key));
		assertNull(map.get(key2));

		assertEquals(v, cacher.getThenPredicateElseSupplier(key, (t) -> {
			throw new RuntimeException("查到null不会进这里");
		}, () -> v, expireSeconds));

		assertEquals(v, cacher.getThenPredicateElseSupplier(key, (t) -> t.equals(v), () -> v2, expireSeconds));// 查到v断定匹配，最后返回v
		assertEquals(v2, cacher.getThenPredicateElseSupplier(key, (t) -> false, () -> v2, expireSeconds));// 虽然查到v但是断定false，最后返回v2
	}

	@Test
	public void testgetThenPredicateElseSupplier2() throws Exception {
		Map<String, Object> map = cacher.get(Arrays.asList(key, key2));
		assertNull(map.get(key));
		assertNull(map.get(key2));

		cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, (t) -> {
			throw new RuntimeException("查到null不会进这里");
		}, () -> v, expireSeconds), Tuples.of(key2, (t) -> {
			throw new RuntimeException("查到null不会进这里");
		}, () -> v2, expireSeconds)));

		map = cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, (t) -> t.equals(v), () -> v2, expireSeconds),
				Tuples.of(key2, (t) -> t.equals(v2), () -> v, expireSeconds)));
		
		assertEquals(v, map.get(key));// 查到v断定匹配，最后返回v
		assertEquals(v2, map.get(key2));// 虽然查到v但是断定false，最后返回v2
	}

	@Test
	public void testSet() throws Exception {
		assertNull(cacher.get(key));

		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);

		assertNull(removes);

		assertEquals(v, cacher.get(key));
		assertEquals(v, cacher.get(key));// 再次读取
	}
	
	@Test
	public void testSetNull() throws Exception {
		try{
			cacher.set(null, null, expireSeconds);
			throw new RuntimeException("expect ex");
		}catch (Exception ignore) {
		}
	}

	@Test
	public void testSetBatch() throws Exception {
		List<Tuple3<String, Object, Integer>> removes = cacher
				.set(Arrays.asList(Tuples.of(key, v, expireSeconds), Tuples.of(key2, v2, expireSeconds)));

		assertNull(removes);

		assertEquals(v, cacher.get(key));
		assertEquals(v, cacher.get(key));// 再次读取
		assertEquals(v2, cacher.get(key2));
		assertEquals(v2, cacher.get(key2));// 再次读取
	}

	@Test
	public void testRemove() throws Exception {
		testSetBatch();

		Tuple3<String, UserForTests, Integer> remove = cacher.remove(key);
		Tuple3<String, UserForTests, Integer> remove2 = cacher.remove(key2);

		if(notnullOnRemoveIfExists()) {
			assertNotNull(remove);
			assertNotNull(remove2);
		}

		remove = cacher.remove(key);
		remove2 = cacher.remove(key2);

		assertNull(remove);// 无法再次remove
		assertNull(remove2);// 无法再次remove

		assertNull(cacher.get(key));// 无法获取
		assertNull(cacher.get(key2));// 无法获取
	}

	@Test
	public void testRemoves() throws Exception {
		testSetBatch();

		List<Tuple3<String, Object, Integer>> removes = cacher.remove(Arrays.asList(key, key2));

		if(notnullOnRemoveIfExists()) {
			assertNotNull(removes);
			assertEquals(2, removes.size());
			assertEquals(v, removes.get(0).getT2());
			assertEquals(v2, removes.get(1).getT2());
		}

		removes = cacher.remove(Arrays.asList(key, key2));

		assertNull(removes);// 无法再次remove

		assertNull(cacher.get(key));// 无法获取
		assertNull(cacher.get(key2));// 无法获取
	}
//	/**
//	 * 并发读写<br>
//	 * @throws Exception
//	 */
//	@Test
//	public void testConcurrentOp() throws Exception {
//		try {
//			Random random = new Random();
//			int threads = 50;
//			int keys = 2000;
//			CountDownLatch countDownLatch = new CountDownLatch(threads);
//			for(int i=0;i<threads;i++) {
//				new Thread() {
//					public void run() {
//						//如果并发不安全
//						try {
//							for(int j=0;j<keys;j++) {
//								final byte[] _KB = new byte[ random.nextInt(1024) + 1 ];
//								cacher.set(j+"", _KB, expireSeconds);
//								cacher.remove(j+"");
//							}
//						} catch (Throwable e) {
//							e.printStackTrace();
//							System.exit(-1);
//						}
//						countDownLatch.countDown();
//					};
//				}.start();
//			}
//			countDownLatch.await(30,TimeUnit.SECONDS);
//			
//			//检查remove之后是否已经真的没有了
//			for(int j=0;j<keys;j++) {
//				assertNull(cacher.get(j+""));
//			}
//		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw e;
//		}
//	}
	
}
