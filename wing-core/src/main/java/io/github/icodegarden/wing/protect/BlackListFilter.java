package io.github.icodegarden.wing.protect;

import java.util.Collection;
import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * @author Fangfang.Xu
 *
 */
public class BlackListFilter extends ShouldFilter {

	private final Collection<String> blacks;

	public BlackListFilter(Collection<String> blacks, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.blacks = blacks;
	}

	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		if (blacks.contains(key)) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by black list");
		}
	}
}
