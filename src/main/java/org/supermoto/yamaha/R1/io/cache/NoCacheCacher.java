package org.supermoto.yamaha.R1.io.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NoCacheCacher implements Cacher{

	@Override
	public <V> V get(String key) {
		return null;
	}
	
	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		return null;
	}

	@Override
	public <V> V getElseSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		return supplier.get();
	}

	@Override
	public <V> V getThenPredicateElseSupplier(String key, Predicate<V> predicate, Supplier<V> supplier,
			int expireSeconds) {
		return supplier.get();
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		return null;
	}
	
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		return null;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		return null;
	}
	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		return null;
	}
}
