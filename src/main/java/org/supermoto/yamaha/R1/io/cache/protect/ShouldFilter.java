package org.supermoto.yamaha.R1.io.cache.protect;

import java.util.function.Predicate;

import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;

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
	public void doFilter(String key) throws RejectedRequestException {
		if (shouldFilter.test(key)) {
			shouldDoFilter(key);
		}
	}

	protected abstract void shouldDoFilter(String key) throws RejectedRequestException;
}
