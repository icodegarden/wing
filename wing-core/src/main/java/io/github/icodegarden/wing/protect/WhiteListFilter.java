package io.github.icodegarden.wing.protect;

import java.util.Collection;
import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * @author Fangfang.Xu
 *
 */
public class WhiteListFilter extends ShouldFilter {

	private final Collection<String> whites;

	public WhiteListFilter(Collection<String> whites, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.whites = whites;
	}

	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		if (!whites.contains(key)) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by white list");
		}
	}
}
