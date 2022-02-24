package org.supermoto.yamaha.R1.io.cache.distribution;

import java.util.Collection;
import java.util.List;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;

/**
 * 分布式缓存标志，RedisCacher<br>
 * 影响面与策略相关<br>
 * 
 * @author Fangfang.Xu
 *
 */
public interface DistributedCacher extends Cacher {

	class ShouldWriteOpHolder {
		private static final ThreadLocal<Boolean> THREADLOCAL = new ThreadLocal<Boolean>();
	}

	/**
	 * 是否允许写操作，没有进行过 {@link #shouldWriteOpThread(boolean)} 则是允许
	 * 
	 * @return
	 */
	default boolean shouldWriteOpThread() {
		Boolean b = ShouldWriteOpHolder.THREADLOCAL.get();
		return b == null ? true : b;
	}

	/**
	 * 设置是否允许写操作
	 * 
	 * @param b
	 */
	static void shouldWriteOpThread(Boolean b) {
		ShouldWriteOpHolder.THREADLOCAL.set(b);
	}

	@Override
	default <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		return shouldWriteOpThread() ? doSet(key, v, expireSeconds) : null;
	}

	<V> List<Tuple3<String, Object, Integer>> doSet(String key, V v, int expireSeconds);

	@Override
	default <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		return shouldWriteOpThread() ? doSet(kvts) : null;
	}

	<V> List<Tuple3<String, Object, Integer>> doSet(List<Tuple3<String, V, Integer>> kvts);

	@Override
	default <V> Tuple3<String, V, Integer> remove(String key) {
		return shouldWriteOpThread() ? doRemove(key) : null;
	}

	<V> Tuple3<String, V, Integer> doRemove(String key);

	@Override
	default <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		return shouldWriteOpThread() ? doRemove(keys) : null;
	}

	<V> List<Tuple3<String, V, Integer>> doRemove(Collection<String> keys);
}
