package io.github.icodegarden.wing.limiter;

public interface RateLimiter {

	/**
	 * 
	 * @param ds limit Dimensions
	 * @return
	 */
	boolean isAllowable(Dimension... ds);
}
