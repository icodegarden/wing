package io.github.icodegarden.wing.protect;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * @author Fangfang.Xu
 *
 */
public class RegexFilter extends ShouldFilter {

	private final String regex;
	private final Pattern p;

	public RegexFilter(String regex, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.regex = regex;
		this.p = Pattern.compile(regex);
	}

	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		Matcher m = p.matcher(key);
		if (!m.matches()) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by regex:" + regex);
		}
	}
}
