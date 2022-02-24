package org.supermoto.yamaha.R1.io.cache.java;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
public class HeapMemoryCacher implements Cacher {

	private Map<String, Object> map = new ConcurrentHashMap<String, Object>();

	@Override
	public <V> V get(String key) {
		return (V) map.get(key);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		Map<String, V> result = new HashMap<String, V>(keys.size(), 1);
		keys.forEach(key -> {
			V v = get(key);
			result.put(key, v);
		});
		return result;
	}
	
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		map.put(key, v);
		return null;
	}
	
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		kvts.forEach(kvt -> {
			set(kvt.getT1(), kvt.getT2(), kvt.getT3());
		});
		return null;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		V remove = (V) map.remove(key);
		if (remove == null) {
			return null;
		}
		return Tuples.of(key, remove, 0);
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		List<Tuple3<String, V, Integer>> collect = keys.stream().map(key -> {
			Tuple3<String, V, Integer> remove = remove(key);
			return remove;
		}).filter(i -> i != null).collect(Collectors.toList());
		return collect.isEmpty() ? null : collect;// 空的时候返回null
	}
}
