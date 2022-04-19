package io.github.icodegarden.wing.protect;

import java.util.function.Function;

import io.github.icodegarden.wing.common.RejectedRequestException;
import io.github.icodegarden.wing.limiter.Dimension;
import io.github.icodegarden.wing.limiter.RateLimiter;

/**
 * <p>
 * 限流保护
 * 
 * @author Fangfang.Xu
 *
 */
public class RateLimitProtector implements Protector {

	private final RateLimiter rateLimiter;
	private final Function<String, Dimension[]> dimensionOffer;

	/**
	 * 
	 * @param rateLimiter
	 * @param dimensionOffer <T,R> T=key
	 */
	public RateLimitProtector(RateLimiter rateLimiter, Function<String, Dimension[]> dimensionOffer) {
		this.rateLimiter = rateLimiter;
		this.dimensionOffer = dimensionOffer;
	}

	@Override
	public <V> V doProtector(ProtectorChain<V> chain) throws RejectedRequestException {
		String key = chain.key();

		Dimension[] dimensions = dimensionOffer.apply(key);
		if (dimensions != null) {
			if (!rateLimiter.isAllowable(dimensions)) {
				throw new RejectedRequestException(this, "Rate Limited on load data when cache not found, key:" + key);
			}
		}
		return chain.doProtector();
	}
}
