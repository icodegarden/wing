package io.github.icodegarden.wing.expire;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.util.NamedThreadFactory;
import io.github.icodegarden.commons.lang.util.ThreadPools;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.metrics.MetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class AutoExpireCacher extends MetricsCacher {
	
	private static Logger log = LoggerFactory.getLogger(AutoExpireCacher.class);

	private static final ScheduledThreadPoolExecutor REMOVE_EXPIRES_THREADPOOL = ThreadPools
			.singleScheduledThreadPool("removeExpires");
	
	private static final int DEFAULT_SCAN_PERIOD_SECONDS = 10;
	private static final NamedThreadFactory CACHE_AUTO_EXPIRE_THREADFACTORY = new NamedThreadFactory(
			"cache-auto-expire");

	public AutoExpireCacher(Cacher cacher) {
		this(cacher, DEFAULT_SCAN_PERIOD_SECONDS);
	}

	public AutoExpireCacher(Cacher cacher, int scanPeriodSeconds) {
		this(cacher, scanPeriodSeconds, false);
	}

	public AutoExpireCacher(Cacher cacher, boolean standalone) {
		this(cacher, DEFAULT_SCAN_PERIOD_SECONDS, standalone);
	}

	public AutoExpireCacher(Cacher cacher, int scanPeriodSeconds, boolean standalone) {
		super(cacher);

		if (standalone) {
			ScheduledExecutorService scheduledExecutor = Executors
					.newSingleThreadScheduledExecutor(CACHE_AUTO_EXPIRE_THREADFACTORY);
			scheduledExecutor.scheduleAtFixedRate(() -> {
				this.removeExpires();
			}, 1, scanPeriodSeconds, TimeUnit.SECONDS);
		} else {
			REMOVE_EXPIRES_THREADPOOL.scheduleAtFixedRate(() -> {
				this.removeExpires();
			}, 1, scanPeriodSeconds, TimeUnit.SECONDS);
		}
	}

	private void removeExpires() {
		try {
			List<String> expiredKeys = keyMetrics.expiredKeys();
			if (log.isDebugEnabled()) {
				log.debug("scan and the expiredKeys:{}", expiredKeys);
			}
			if (!expiredKeys.isEmpty()) {
				remove(expiredKeys);
			}
		} catch (Exception e) {
			log.error("remove expired keys failed", e);
		}
	}

	/**
	 * expireSeconds如果不大于0，则认为已过期，不需要缓存
	 */
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		if (expireSeconds > 0) {
			return super.set(key, v, expireSeconds);
		}
		return null;
	}
	/**
	 * expireSeconds如果不大于0，则认为已过期，不需要缓存
	 */
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, V, Integer>> filtered = kvts.stream().filter(kvt -> kvt.getT3() > 0)
				.collect(Collectors.toList());
		if (!filtered.isEmpty()) {
			return super.set(filtered);
		}
		return null;
	}
}