package org.supermoto.yamaha.R1.io.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import org.supermoto.yamaha.R1.io.cache.metrics.MetricsCacher;

import io.github.icodegarden.commons.lang.Delegatable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CapacityLimitedCacher implements Cacher {
	
	private static Logger log = LoggerFactory.getLogger(CapacityLimitedCacher.class);

	private final MetricsCacher cacher;

	public CapacityLimitedCacher(MetricsCacher cacher) {
		this.cacher = cacher;
	}
	
	@Override
	public Delegatable getDelegatable() {
		return cacher;
	}

	public MetricsCacher getMetricsCacher() {
		return cacher;
	}
	
	@Override
	public <V> V get(String key) {
		return cacher.get(key);
	}
	
	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		return cacher.get(keys);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		List<Tuple3<String, Object, Integer>> removeIfThresholds = removeIfThresholds(
				Arrays.asList(Tuples.of(key, v, expireSeconds)));

		cacher.set(key, v, expireSeconds);

		return removeIfThresholds;
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, Object, Integer>> removeIfThresholds = removeIfThresholds(kvts);

		cacher.set(kvts);

		return removeIfThresholds;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		return cacher.remove(key);
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		return cacher.remove(keys);
	}

	/**
	 * @param <V>
	 * @param kvts
	 * @return removed
	 */
	private <V> List<Tuple3<String, Object, Integer>> removeIfThresholds(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, Object, Integer>> removes = removePreSet(kvts);
		if (removes != null && !removes.isEmpty()) {
			// 移除并把存活的重新设为0，否则新的永远追不上旧的，旧的始终存活
			cacher.resetUsedTimes();
		}
		return removes;
	}

	/**
	 * 当待进入的缓存超过容量限制时移除需要移除的缓存
	 * 
	 * @param <V>
	 * @param kvts 待进入的缓存
	 * @return removed , nullable
	 */
	protected abstract <V> List<Tuple3<String, Object, Integer>> removePreSet(List<Tuple3<String, V, Integer>> kvts);

	protected <V> List<Tuple3<String, Object, Integer>> lruOfAvg(int requestKeySize) {
		long average = cacher.usedTimesAvg();
		if (log.isDebugEnabled()) {
			log.debug("cal remove avg is:{}", average);
		}
		// 小于等于avg的
		Collection<String> removeKeys = cacher.keysUsedTimesLte(average);

		// 担保
		if (removeKeys.size() < requestKeySize) {
			removeKeys = cacher.keys();
		}
		if (log.isDebugEnabled()) {
			log.debug("Thresholds to remove keys:{}", removeKeys);
		}
		if (removeKeys.isEmpty()) {
			return null;
		}
		return remove(removeKeys);
	}
}
