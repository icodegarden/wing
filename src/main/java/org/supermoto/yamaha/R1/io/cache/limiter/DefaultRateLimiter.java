package org.supermoto.yamaha.R1.io.cache.limiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultRateLimiter implements RateLimiter {

	private static final Logger log = LoggerFactory.getLogger(DefaultRateLimiter.class);

	private final int maxDimensionSize;

	private final ConcurrentMap<String, Dimension> dimensions = new ConcurrentHashMap<String, Dimension>();

	public DefaultRateLimiter() {
		this(16384);
	}

	public DefaultRateLimiter(int maxDimensionSize) {
		this.maxDimensionSize = maxDimensionSize;
	}

	@Override
	public boolean isAllowable(Dimension... ds) {
		for (Dimension d : ds) {
			String name = d.getName();
			Dimension exist = dimensions.get(name);
			if (exist == null) {
				clearIfRequired(3000);
				dimensions.putIfAbsent(name, d.start());
				exist = dimensions.get(name);
			} else {
				if (exist.getRate() != d.getRate() || exist.getInterval() != d.getInterval()) {
					dimensions.put(name, d.start());
					exist = dimensions.get(name);
				}
			}
			if (!exist.isAllowable()) {
				if (log.isInfoEnabled()) {
					log.info("tps limit {} not allowed", exist.getName());
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param unactiveMillis 允许为负
	 */
	void clearIfRequired(long unactiveMillis) {
		if (dimensions.size() > maxDimensionSize) {
			dimensions.entrySet().removeIf(
					entry -> entry.getValue().getLastResetTime() < (System.currentTimeMillis() - unactiveMillis)// 清理n毫秒不活跃的
			);
			clearIfRequired(unactiveMillis - 1000);
		}
	}

	ConcurrentMap<String, Dimension> getDimensions() {
		return dimensions;
	}
}
