package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.CacherTests;
import io.github.icodegarden.wing.common.RejectedRequestException;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.limiter.DefaultRateLimiter;
import io.github.icodegarden.wing.limiter.Dimension;
import io.github.icodegarden.wing.protect.BlackListFilter;
import io.github.icodegarden.wing.protect.OverloadProtectionCacher;
import io.github.icodegarden.wing.protect.RateLimitProtector;
import io.github.icodegarden.wing.protect.SynchronizedDoubleCheckProtector;
import io.github.icodegarden.wing.protect.WhiteListFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class OverloadProtectionCacherTests extends CacherTests {

	@Override
	protected Cacher getCacher() {
		OverloadProtectionCacher cacher = new OverloadProtectionCacher(new HeapMemoryCacher(), null, null);
		return cacher;
	}

	@Test
	public void testFilters() throws Exception {
		WhiteListFilter f1 = new WhiteListFilter(Arrays.asList(key, key3), v -> true);
		BlackListFilter f2 = new BlackListFilter(Arrays.asList(key2, key3), v -> true);
		OverloadProtectionCacher cacher = new OverloadProtectionCacher(new HeapMemoryCacher(), Arrays.asList(f1, f2),
				null);

		/**
		 * 测试
		 */
		assertEquals(v, cacher.getElseSupplier(key, () -> v, expireSeconds));
		try {
			cacher.getElseSupplier(key2, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}
		try {
			cacher.getElseSupplier(key3, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getThenPredicateElseSupplier(key, v -> false, () -> v, expireSeconds);
		try {
			cacher.getThenPredicateElseSupplier(key2, v -> false, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}
		try {
			cacher.getThenPredicateElseSupplier(key3, v -> false, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getElseSupplier(Arrays.asList(Tuples.of(key, () -> v, expireSeconds)));
		try {
			// key2 key3被拦截
			cacher.getElseSupplier(Arrays.asList(Tuples.of(key, () -> v, expireSeconds),
					Tuples.of(key2, () -> v2, expireSeconds), Tuples.of(key3, () -> v3, expireSeconds)));
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, v -> false, () -> v, expireSeconds)));
		try {
			// key2 key3被拦截
			cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, v -> false, () -> v, expireSeconds),
					Tuples.of(key2, v -> false, () -> v2, expireSeconds),
					Tuples.of(key3, v -> false, () -> v3, expireSeconds)));
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}
	}

	@Test
	public void testProtectors() throws Exception {
		Dimension[] ds = new Dimension[] {new Dimension("global", 1, 1000)};
		RateLimitProtector p1 = new RateLimitProtector(new DefaultRateLimiter(), key->ds) ;
		SynchronizedDoubleCheckProtector p2 = new SynchronizedDoubleCheckProtector();
		OverloadProtectionCacher cacher = new OverloadProtectionCacher(new HeapMemoryCacher(), null,
				Arrays.asList(p1, p2));

		/**
		 * 测试
		 */
		assertEquals(v, cacher.getElseSupplier(key, () -> v, expireSeconds));
		try {
			cacher.getElseSupplier(key2, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		Thread.sleep(1100);
		
		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getThenPredicateElseSupplier(key, v -> false, () -> v, expireSeconds);
		try {
			cacher.getThenPredicateElseSupplier(key2, v -> false, () -> v, expireSeconds);
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		Thread.sleep(1100);
		
		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getElseSupplier(Arrays.asList(Tuples.of(key, () -> v, expireSeconds)));
		try {
			cacher.getElseSupplier(Arrays.asList(Tuples.of(key, () -> v, expireSeconds),
					Tuples.of(key2, () -> v2, expireSeconds), Tuples.of(key3, () -> v3, expireSeconds)));
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}

		Thread.sleep(1100);
		
		cacher.remove(Arrays.asList(key,key2,key3));
		
		/**
		 * 测试
		 */
		cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, v -> false, () -> v, expireSeconds)));
		try {
			cacher.getThenPredicateElseSupplier(Arrays.asList(Tuples.of(key, v -> false, () -> v, expireSeconds),
					Tuples.of(key2, v -> false, () -> v2, expireSeconds),
					Tuples.of(key3, v -> false, () -> v3, expireSeconds)));
			throw new AssertionError("期望异常");
		} catch (RejectedRequestException e) {
		}
	}
}