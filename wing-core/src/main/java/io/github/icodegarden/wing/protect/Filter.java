package io.github.icodegarden.wing.protect;

import io.github.icodegarden.wing.common.RejectedCacheException;

/**
 * <p>
 * 应对穿透等
 * @author Fangfang.Xu
 *
 */
public interface Filter {

	void doFilter(String key) throws RejectedCacheException;
}
