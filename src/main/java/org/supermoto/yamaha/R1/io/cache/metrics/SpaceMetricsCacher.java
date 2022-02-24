package org.supermoto.yamaha.R1.io.cache.metrics;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.supermoto.yamaha.R1.io.cache.SpaceCalcableCacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;

/**
 * 
 * 不考虑并发安全，场景不会要求非常精确 {@link SpaceMetricsCacher#set} 等等...<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class SpaceMetricsCacher extends MetricsCacher {

	private AtomicLong usedSpaceSize = new AtomicLong();

	final SpaceCalcableCacher cacher;

	public SpaceMetricsCacher(SpaceCalcableCacher cacher) {
		super(cacher);
		this.cacher = cacher;
	}

	public long usedSpaceSize() {
		return usedSpaceSize.get();
	}

	public int spaceSize(Object v) {
		return cacher.spaceSizeCalc(v);
	}

	private <V> void addUsedSpaceSize(int spaceSize) {
		usedSpaceSize.addAndGet(spaceSize);
	}
	
	private void reduceUsedSpaceSize(int spaceSize) {
		usedSpaceSize.addAndGet(-spaceSize);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		Integer preSpaceSize = cacher.spaceSize(key);//可能的旧值

		List<Tuple3<String, Object, Integer>> removes = super.set(key, v, expireSeconds);

		if (preSpaceSize != null) {
			reduceUsedSpaceSize(preSpaceSize);
		}

		Integer spaceSize = cacher.spaceSize(key);//新值
		if(spaceSize != null) {
			addUsedSpaceSize(spaceSize);
		}
		
		return removes;
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		final int preSpaceSize = kvts.stream().mapToInt(kvt -> {
			Integer size = cacher.spaceSize(kvt.getT1());
			return size != null ? size : 0;
		}).sum();

		List<Tuple3<String, Object, Integer>> removes = super.set(kvts);

		if (preSpaceSize != 0) {
			reduceUsedSpaceSize(preSpaceSize);
		}

		int spaceSize = kvts.stream().mapToInt(kvt -> {
			Integer size = cacher.spaceSize(kvt.getT1());
			return size != null ? size : 0;
		}).sum();

		if (spaceSize != 0) {
			addUsedSpaceSize(spaceSize);
		}

		return removes;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		Integer preSpaceSize = cacher.spaceSize(key);// 可能的旧值

		Tuple3<String, V, Integer> remove = super.remove(key);

		if (preSpaceSize != null) {
			reduceUsedSpaceSize(preSpaceSize);
		}
		return remove;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		int preSpaceSize = keys.stream().mapToInt(key -> {
			Integer size = cacher.spaceSize(key);
			return size != null ? size : 0;
		}).sum();

		List<Tuple3<String, V, Integer>> removes = super.remove(keys);

		if (preSpaceSize != 0) {
			reduceUsedSpaceSize(preSpaceSize);
		}

		return removes;
	}

}