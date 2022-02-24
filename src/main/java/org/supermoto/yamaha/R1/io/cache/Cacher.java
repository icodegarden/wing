package org.supermoto.yamaha.R1.io.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuple4;

import io.github.icodegarden.commons.lang.Delegatable;

/**
 * 如果期望expireSeconds有效，请使用
 * {@link org.supermoto.yamaha.R1.io.cache.expire.AutoExpireCacher} <br>
 * 
 * @author Fangfang.Xu
 *
 */
public interface Cacher extends Delegatable {

	/**
	 * 
	 * @param <V>
	 * @param key
	 * @return Nullable , value of cache
	 */
	<V> V get(String key);

	/**
	 * 
	 * @param <V>
	 * @param keys
	 * @return NotNull , 约定key数量与入参数量相同，对应的value可能为null
	 */
	<V> Map<String, V> get(Collection<String> keys);

	/**
	 * always use Supplier
	 * 
	 * @param <V>
	 * @param key
	 * @param supplier
	 * @param expireSeconds
	 * @return
	 */
	default <V> V fromSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		V v = supplier.get();
		if (v != null) {
			set(key, v, expireSeconds);
		}
		return v;
	}

	/**
	 * 
	 * @param <V>
	 * @param key
	 * @param supplier      当缓存不存在时的提供者
	 * @param expireSeconds
	 * @return
	 */
	default <V> V getElseSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		V get = get(key);
		if (get == null) {
			get = fromSupplier(key, supplier, expireSeconds);
		}
		return get;
	}

	/**
	 * 
	 * @param <V>
	 * @param key
	 * @param predicate     验证缓存是否符合预期
	 * @param supplier      当缓存不存在或predicate结果是false时的提供者
	 * @param expireSeconds
	 * @return
	 */
	default <V> V getThenPredicateElseSupplier(String key, Predicate<V> predicate, Supplier<V> supplier,
			int expireSeconds) {
		V get = get(key);
		return Optional.ofNullable(get).filter(predicate).orElseGet(() -> fromSupplier(key, supplier, expireSeconds));
	}

	/**
	 * @param <V>
	 * @param kvts <key,Supplier,expireSeconds>
	 * @return key=value
	 */
	default <V> Map<String, V> getElseSupplier(Collection<Tuple3<String, Supplier<V>, Integer>> kvts) {
		Map<String, Tuple3<String, Supplier<V>, Integer>> pairs = kvts.stream()
				.collect(Collectors.toMap(Tuple3::getT1, i -> i));
		Set<String> keys = pairs.keySet();

		Map<String, V> ret = get(keys);
		for (String key : keys) {
			V v = ret.get(key);
			if (v == null) {
				Tuple3<String, Supplier<V>, Integer> kvt = pairs.get(key);
				v = fromSupplier(key, kvt.getT2(), kvt.getT3());
				if (v != null) {
					ret.put(key, v);
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param <V>
	 * @param kvts <key,Predicate,Supplier,expireSeconds>
	 * @return key=value
	 */
	default <V> Map<String, V> getThenPredicateElseSupplier(
			Collection<Tuple4<String, Predicate<V>, Supplier<V>, Integer>> kvts) {
		Map<String, Tuple4<String, Predicate<V>, Supplier<V>, Integer>> pairs = kvts.stream()
				.collect(Collectors.toMap(Tuple4::getT1, i -> i));
		Set<String> keys = pairs.keySet();

		Map<String, V> ret = get(keys);
		for (String key : keys) {
			V v = ret.get(key);

			Tuple4<String, Predicate<V>, Supplier<V>, Integer> kvt = pairs.get(key);

			if (v == null) {
				v = fromSupplier(key, kvt.getT3(), kvt.getT4());
			} else {
				v = Optional.ofNullable(v).filter(kvt.getT2())
						.orElseGet(() -> fromSupplier(key, kvt.getT3(), kvt.getT4()));
			}
			if (v != null) {
				ret.put(key, v);
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param <V>
	 * @param key
	 * @param v
	 * @param expireSeconds
	 * @return removes
	 *         由于新的缓存被设置而需要被移除的部分，通常由于空间限制问题需要移除，如果没有空间问题不应有返回值，不应在新v替换旧v时有返回值
	 */
	<V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds);

	/**
	 * 
	 * @param <V>
	 * @param kvts
	 * @return removes
	 *         由于新的缓存被设置而需要被移除的部分，通常由于空间限制问题需要移除，如果没有空间问题不应有返回值，不应在新v替换旧v时有返回值
	 */
	<V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts);

	/**
	 * 
	 * @param <V>
	 * @param key
	 * @return Nullable
	 */
	<V> Tuple3<String, V, Integer> remove(String key);

	/**
	 * 
	 * @param <V>
	 * @param keys
	 * @return Nullable, 不会返回空集合而是null
	 */
	<V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys);
}
