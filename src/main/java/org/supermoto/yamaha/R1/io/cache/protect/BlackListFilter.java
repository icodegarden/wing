package org.supermoto.yamaha.R1.io.cache.protect;

import java.util.Collection;
import java.util.function.Predicate;

import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;

/**
 * @author Fangfang.Xu
 *
 */
public class BlackListFilter extends ShouldFilter {

	private final Collection<String> list;

	public BlackListFilter(Collection<String> list, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.list = list;
	}

	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		if (list.contains(key)) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by black list");
		}
	}
}
