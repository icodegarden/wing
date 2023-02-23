package io.github.icodegarden.wing.level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.common.ArgumentCacheException;
import io.github.icodegarden.wing.distribution.DistributedCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LevelableCacher implements Cacher {

	private final Level level;
	private final SetOfFromStrategy setOfFromStrategy;
	private final OutOfLimitStrategy outOfLimitStrategy;
	private final GetOfUpgradeStrategy upgradeStrategy;

	/**
	 * 
	 * @param orderedCachers 第一个就是L1,依次递增
	 */
	public LevelableCacher(List<Cacher> ordered, SetOfFromStrategy setOfFromStrategy,
			OutOfLimitStrategy outOfLimitStrategy, GetOfUpgradeStrategy upgradeStrategy) {
		if (ordered == null || ordered.isEmpty()) {
			throw new ArgumentCacheException("ordered must not empty");
		}
		if (setOfFromStrategy == null) {
			throw new ArgumentCacheException(SetOfFromStrategy.class.getName() + " must not null");
		}
		if (outOfLimitStrategy == null) {
			throw new ArgumentCacheException(OutOfLimitStrategy.class.getName() + " must not null");
		}
		if (upgradeStrategy == null) {
			throw new ArgumentCacheException(GetOfUpgradeStrategy.class.getName() + " must not null");
		}
		level = Level.of(ordered);
		this.setOfFromStrategy = setOfFromStrategy;
		this.outOfLimitStrategy = outOfLimitStrategy;
		this.upgradeStrategy = upgradeStrategy;
	}

	public int numberOfLevels() {
		int numberOfLevels = 0;
		Level l = this.level;
		while (l != null) {
			numberOfLevels++;
			l = l.getNext();
		}
		return numberOfLevels;
	}

	public Level getLevel(int number) throws IndexOutOfBoundsException {
		Level l = this.level;
		for (int i = 0; i < number - 1; i++) {
			if (l == null) {
				throw new IndexOutOfBoundsException();
			}
			l = l.getNext();
		}
		if (l == null) {
			throw new IndexOutOfBoundsException();
		}
		return l;
	}

	@Override
	public <V> V get(String key) {
		Level l = this.level;
		while (l != null) {
			V v = l.getCacher().get(key);
			if (v != null) {
				upgradeStrategy.upgrade(l, key, v, outOfLimitStrategy);
				return v;
			}
			l = l.getNext();
		}
		return null;
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		Map<String, V> ret = null;

		Level l = this.level;
		while (l != null) {
			Level fl = l;
			Map<String, V> map = l.getCacher().get(keys);
			/**
			 * 对获取到的值进行upgrade<br>
			 * 过滤出没有值的keys<br>
			 */
			List<String> noValueKeys = map.entrySet().stream().filter(entry -> {
				String key = entry.getKey();
				Object v = entry.getValue();
				if (v != null) {
					upgradeStrategy.upgrade(fl, key, v, outOfLimitStrategy);
					return false;
				}
				return true;
			}).map(Map.Entry::getKey).collect(Collectors.toList());

			keys = noValueKeys;// 没有值的keys继续查找

			l = l.getNext();

			if (ret == null) {
				ret = map;// 首次的map将会包含所有的keys
			} else {
				ret.putAll(map);// 追加到结果
			}
		}
		return ret;
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		Level selected = setOfFromStrategy.select(level);
		List<Tuple3<String, Object, Integer>> remove = selected.getCacher().set(key, v, expireSeconds);
		return outOfLimitStrategy.set(selected, remove);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		Level selected = setOfFromStrategy.select(level);
		List<Tuple3<String, Object, Integer>> removes = selected.getCacher().set(kvts);
		return outOfLimitStrategy.set(selected, removes);
	}

	/**
	 * <p>
	 * 当removed 还是 null 时一律执行
	 * <p>
	 * 当removed 不是 null
	 * 时，如果是DistributedCacher类型则执行。原因：{@link io.github.icodegarden.wing.level.GetOfUpgradeStrategy#UpgradeGtMinAvg}
	 * 中 {@link io.github.icodegarden.wing.distribution.DistributedCacher} 不会删除
	 */
	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		Level l = this.level;
		Tuple3<String, V, Integer> removed = null;
		while (l != null) {
			if (removed == null) {
				Tuple3<String, V, Integer> remove = l.getCacher().remove(key);
				removed = remove;
			} else if (l.getCacher() instanceof DistributedCacher) {
				l.getCacher().remove(key);
			}

			l = l.getNext();
		}
		return removed;
	}

	/**
	 * 不同key可能分布在不同层级上,因此层层执行
	 */
	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		List<Tuple3<String, V, Integer>> removesList = new ArrayList<Tuple3<String, V, Integer>>(keys.size());

		Level l = this.level;
		while (l != null) {
			List<Tuple3<String, V, Integer>> removes = l.getCacher().remove(keys);
			if (removes != null) {
				// 先删除可能的重复的
				removesList.removeIf(i -> {
					for (Tuple3<String, V, Integer> remove : removes) {
						if (remove.getT1().equals(i.getT1())) {
							return true;
						}
					}
					return false;
				});
				// 再加进来
				removesList.addAll(removes);
			}
			l = l.getNext();
		}
		return removesList;
	}
}
