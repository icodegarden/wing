package io.github.icodegarden.wing.protect;

import java.util.function.Function;

import io.github.icodegarden.commons.lang.limiter.RateLimiter;
import io.github.icodegarden.wing.common.RejectedCacheException;

/**
 * <p>
 * 限流保护
 * 
 * @author Fangfang.Xu
 *
 */
public class RateLimitProtector implements Protector {

	private final Function<String, RateLimiter> rateLimiterOffer;

	/**
	 * 
	 * @param rateLimiter
	 * @param dimensionOffer <T,R> T=key
	 */
	public RateLimitProtector(RateLimiter rateLimiter) {
		this(new Function<String, RateLimiter>() {
			@Override
			public RateLimiter apply(String t) {
				return rateLimiter;
			}
		});
	}

	public RateLimitProtector(Function<String/*cache key*/, RateLimiter> rateLimiterOffer) {
		this.rateLimiterOffer = rateLimiterOffer;
	}

	@Override
	public <V> V doProtector(ProtectorChain<V> chain) throws RejectedCacheException {
		String key = chain.key();

		RateLimiter rateLimiter = rateLimiterOffer.apply(key);

		if (rateLimiter != null) {
			if (!rateLimiter.isAllowable()) {
				throw new RejectedCacheException(this, "RateLimited on load data when cache not found, key:" + key);
			}
		}
		return chain.doProtector();
	}
}
