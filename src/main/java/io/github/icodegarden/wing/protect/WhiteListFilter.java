package io.github.icodegarden.wing.protect;

import java.util.Collection;
import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * @author Fangfang.Xu
 *
 */
public class WhiteListFilter extends ShouldFilter {

	private final Collection<String> list;

	public WhiteListFilter(Collection<String> list, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.list = list;
	}

	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		if (!list.contains(key)) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by white list");
		}
	}
}
