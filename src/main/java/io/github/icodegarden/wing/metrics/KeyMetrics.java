package io.github.icodegarden.wing.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.util.ThreadPools;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class KeyMetrics {
	private static final Logger log = LoggerFactory.getLogger(KeyMetrics.class);

	private final ScheduledThreadPoolExecutor metricsThreadPool = ThreadPools
			.singleScheduledThreadPool("metrics");
	
	private final Map<String, KeyOf> metrics = new ConcurrentHashMap<String, KeyOf>(64);
	private long usedTimesAvg;
	private List<String> expiredKeys = Collections.emptyList();

	public KeyMetrics() {
		metricsThreadPool.scheduleWithFixedDelay(() -> {
			try {
				double average = metrics.values().stream().mapToLong(KeyMetrics -> KeyMetrics.getUsedTimes().get())
						.average().orElse(0);
				usedTimesAvg = (long) average;
			} catch (Exception e) {
				log.error("calc avgOfUsedTimes error", e);
			}
		}, 1, 5, TimeUnit.SECONDS);

		metricsThreadPool.scheduleWithFixedDelay(() -> {
			try {
				final long now = System.currentTimeMillis();
				final long _1s_of_millis = 1000L;
				expiredKeys = metrics.values().stream().filter(keyOf -> {
					return keyOf.getCreatedAt() + keyOf.getExpireSeconds() * _1s_of_millis < now;
				}).map(KeyOf::getKey).collect(Collectors.toList());
			} catch (Exception e) {
				log.error("calc expiredKeys error", e);
			}
		}, 1, 5, TimeUnit.SECONDS);
	}

	class KeyOf {
		final private String key;
		final private long createdAt = System.currentTimeMillis();
		final private int expireSeconds;
		final private AtomicLong usedTimes = new AtomicLong();

		private KeyOf(String key, int expireSeconds) {
			this.key = key;
			this.expireSeconds = expireSeconds;
		}

		public String getKey() {
			return key;
		}

		public long getCreatedAt() {
			return createdAt;
		}

		public int getExpireSeconds() {
			return expireSeconds;
		}

		public AtomicLong getUsedTimes() {
			return usedTimes;
		}

		public String toString() {
			return "[key=" + key + ", expireSeconds=" + expireSeconds + ", usedTimes=" + usedTimes + "]";
		}
	}

	public KeyOf keyOf(String key) {
		return metrics.get(key);
	}

	public KeyOf set(String key, int expireSeconds) {
		KeyOf keyOf = new KeyOf(key, expireSeconds);
		metrics.put(key, keyOf);
		return keyOf;
	}

	public KeyOf remove(String key) {
		return metrics.remove(key);
	}

	public void remove(Collection<String> keys) {
		keys.forEach(key -> {
			metrics.remove(key);
		});
	}

	public void incrementUsedTimes(String key) {
		KeyOf keyOf = metrics.get(key);
		/**
		 * 特殊情况:分布式缓存已有->滞后的实例get到之后进行incrementUsedTimes->metrics中是没有该key的
		 */
		if (keyOf == null) {
			/**
			 * FIXME
			 * 这个时间影响的是进入expiredKeys的时间，分布式缓存实际上是可以设置的很大，因为自动过期由缓存系统自己管理，而不需要进入expiredKeys
			 */
			keyOf = set(key, 3600);
		}
		keyOf.getUsedTimes().getAndIncrement();
	}

	public long keySize() {
		return metrics.size();
	}

	/**
	 * 
	 * @param key
	 * @return millis
	 */
	public long createdAt(String key) {
		KeyOf keyOf = metrics.get(key);
		return keyOf.getCreatedAt();
	}

	public long usedTimes(String key) {
		KeyOf keyOf = metrics.get(key);
		return keyOf != null ? keyOf.getUsedTimes().get() : 0;
	}

	public long usedTimesAvg() {
		return usedTimesAvg;
	}

	public void resetUsedTimes() {
		metrics.values().forEach(KeyMetrics -> {
			KeyMetrics.getUsedTimes().set(0);
		});
	}

	public Collection<String> keys() {
		return metrics.keySet();
	}

	public Collection<String> keysUsedTimesLte(long lte) {
		return metrics.values().stream().filter(keyMertrics -> keyMertrics.getUsedTimes().get() <= lte)
				.map(KeyOf::getKey).collect(Collectors.toList());
	}

	public int expireSeconds(String key) {
		KeyOf KeyMetrics = metrics.get(key);
		return KeyMetrics != null ? KeyMetrics.getExpireSeconds() : 0;
	}

	/**
	 * 
	 * @return not null
	 */
	public List<String> expiredKeys() {
		return expiredKeys;
	}
}