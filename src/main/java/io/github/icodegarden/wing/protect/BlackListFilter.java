package io.github.icodegarden.wing.protect;

import java.util.Collection;
import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedRequestException;

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
