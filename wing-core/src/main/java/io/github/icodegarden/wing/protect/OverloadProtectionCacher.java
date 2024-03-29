package io.github.icodegarden.wing.protect;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.common.RejectedCacheException;

/**
 * <p>
 * 针对的是loadDB，即Supplier
 * <p>
 * 如果被拒绝性的保护，则会throws RejectedRequestException
 * 
 * @author Fangfang.Xu
 *
 */
public class OverloadProtectionCacher implements Cacher {

	private final Cacher cacher;
	private final List<Filter> filters;
	private final List<Protector> protectors;

	/**
	 * 
	 * @param cacher
	 * @param filters    ordered,Nullable
	 * @param protectors ordered,Nullable
	 */
	public OverloadProtectionCacher(Cacher cacher, List<Filter> filters, List<Protector> protectors) {
		if (cacher == null) {
			throw new IllegalArgumentException("cacher must not null");
		}
		this.cacher = cacher;
		this.filters = filters;
		this.protectors = protectors;
	}

	@Override
	public <V> V fromSupplier(String key, Supplier<V> supplier, int expireSeconds) {
		doFilters(key);
		return doProtectors(key, supplier, expireSeconds);
	}

	private void doFilters(String key) throws RejectedCacheException {
		if (filters != null && !filters.isEmpty()) {
			for (Filter filter : filters) {
				filter.doFilter(key);
			}
		}
	}

	private <V> V doProtectors(String key, Supplier<V> supplier, int expireSeconds) {
		if (protectors != null && !protectors.isEmpty()) {
			ProtectorChain<V> chain = new ProtectorChain.Default<V>(cacher, key, supplier, expireSeconds, protectors);
			V v = chain.doProtector();
			if (v != null) {
				set(key, v, expireSeconds);
			}
			return v;
		}
		return Cacher.super.fromSupplier(key, supplier, expireSeconds);
	}

	@Override
	public <V> V get(String key) {
		return cacher.get(key);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		return cacher.get(keys);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		return cacher.set(key, v, expireSeconds);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		return cacher.set(kvts);
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		return cacher.remove(key);
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		return cacher.remove(keys);
	}
}
