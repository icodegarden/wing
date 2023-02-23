package io.github.icodegarden.wing.protect;

import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedCacheException;

/**
 * @author Fangfang.Xu
 *
 */
public abstract class ShouldFilter implements Filter {

	private final Predicate<String> shouldFilter;

	public ShouldFilter(Predicate<String> shouldFilter) {
		this.shouldFilter = shouldFilter;
	}

	@Override
	public void doFilter(String key) throws RejectedCacheException {
		if (shouldFilter.test(key)) {
			shouldDoFilter(key);
		}
	}

	protected abstract void shouldDoFilter(String key) throws RejectedCacheException;
}
