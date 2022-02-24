package org.supermoto.yamaha.R1.io.cache.protect;

import org.supermoto.yamaha.R1.io.cache.common.RejectedRequestException;

/**
 * <p>
 * 应对穿透等
 * @author Fangfang.Xu
 *
 */
public interface Filter {

	void doFilter(String key) throws RejectedRequestException;
}
