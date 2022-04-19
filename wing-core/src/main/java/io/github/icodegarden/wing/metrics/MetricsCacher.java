package io.github.icodegarden.wing.metrics;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.metrics.KeyMetrics.KeyOf;
import io.github.icodegarden.commons.lang.Delegatable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class MetricsCacher implements Cacher {

	protected final KeyMetrics keyMetrics;
	private final Cacher cacher;

	public MetricsCacher(Cacher cacher) {
		this.cacher = cacher;

		/**
		 * 多层复用
		 */
		if (this.cacher instanceof MetricsCacher) {
			this.keyMetrics = ((MetricsCacher) this.cacher).keyMetrics;
		} else {
			keyMetrics = new KeyMetrics();
		}
	}

	@Override
	public Delegatable getDelegatable() {
		return cacher;
	}

	public long keySize() {
		return keyMetrics.keySize();
	}

	public int remainExpireSeconds(String key) {
		KeyOf keyOf = keyMetrics.keyOf(key);
		if (keyOf == null) {
			return 0;
		}
		final int expireSeconds = keyOf.getExpireSeconds();
		final long createdAt = keyOf.getCreatedAt();
		final int remainExpireSeconds = expireSeconds - (int) (System.currentTimeMillis() - createdAt) / 1000;
		return remainExpireSeconds < 0 ? 0 : remainExpireSeconds;
	}

	public long usedTimes(String key) {
		return keyMetrics.usedTimes(key);
	}

	public long usedTimesAvg() {
		return keyMetrics.usedTimesAvg();
	}

	public void resetUsedTimes() {
		keyMetrics.resetUsedTimes();
	}

	public Collection<String> keys() {
		return keyMetrics.keys();
	}

	public Collection<String> keysUsedTimesLte(long lte) {
		return keyMetrics.keysUsedTimesLte(lte);
	}

	public int expireSeconds(String key) {
		return keyMetrics.expireSeconds(key);
	}

	@Override
	public <V> V get(String key) {
		V v = cacher.get(key);
		if (v != null && !(cacher instanceof MetricsCacher)) {
			keyMetrics.incrementUsedTimes(key);
		}
		return v;
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		Map<String, V> ret = cacher.get(keys);
		if (!(cacher instanceof MetricsCacher)) {
			ret.entrySet().forEach(entry -> {
				if (entry.getValue() != null) {
					keyMetrics.incrementUsedTimes(entry.getKey());
				}
			});
		}
		return ret;
	}

	/**
	 * 不用控制并发安全<br>
	 * 并发set相同key的场景不应该有，用户自己需要清楚
	 * 
	 * @author Fangfang.Xu
	 */
	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);

		if (!(cacher instanceof MetricsCacher)) {
			keyMetrics.set(key, expireSeconds);
		}

		return removes;
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, Object, Integer>> removes = cacher.set(kvts);

		if (!(cacher instanceof MetricsCacher)) {
			kvts.forEach(kvt -> {
				String key = kvt.getT1();
				Integer expireSeconds = kvt.getT3();
				keyMetrics.set(key, expireSeconds);
			});
		}

		return removes;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		Tuple3<String, V, Integer> remove = cacher.remove(key);
		if (remove != null) {
			KeyOf keyOf = keyMetrics.remove(key);
			return buildRemove(remove, keyOf);
		}
		return null;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		List<Tuple3<String, V, Integer>> removes = cacher.remove(keys);
		if (removes != null) {
			removes = removes.stream().map(remove -> {
				KeyOf keyOf = keyMetrics.remove(remove.getT1());
				return buildRemove(remove, keyOf);
			}).collect(Collectors.toList());

			return removes;
		}
		return null;
	}

	private <V> Tuple3<String, V, Integer> buildRemove(Tuple3<String, V, Integer> remove, KeyOf keyOf) {
		if (keyOf != null) {
			int remainingExpireSeconds = 0;
			if (keyOf.getExpireSeconds() > 0) {
				// 剩余时间=过期时间-已过去时间
				remainingExpireSeconds = keyOf.getExpireSeconds()
						- (int) (System.currentTimeMillis() - keyOf.getCreatedAt()) / 1000;
				remainingExpireSeconds = remainingExpireSeconds > 0 ? remainingExpireSeconds : 0;
			}
			return Tuples.of(remove.getT1(), remove.getT2(), remainingExpireSeconds);
		}
		return remove;
	}
}