package org.supermoto.yamaha.R1.io.cache.limiter;

public interface RateLimiter {

	/**
	 * 
	 * @param ds limit Dimensions
	 * @return
	 */
	boolean isAllowable(Dimension... ds);
}
