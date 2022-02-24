package org.supermoto.yamaha.R1.io.cache.level;

import java.util.List;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import org.supermoto.yamaha.R1.io.cache.distribution.DistributedCacher;
import org.supermoto.yamaha.R1.io.cache.metrics.MetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface GetOfUpgradeStrategy {
	/**
	 * 
	 * @param <V>
	 * @param level 对应key,v缓存数据的层级
	 * @param key 需要上升部分
	 * @param v 需要上升部分
	 * @param outOfLimitStrategy
	 * @return whether upgrade
	 */
	<V> boolean upgrade(Level level, String key, V v, OutOfLimitStrategy outOfLimitStrategy);

	public class NoOp implements GetOfUpgradeStrategy {
		@Override
		public <V> boolean upgrade(Level level, String key, V v, OutOfLimitStrategy outOfLimitStrategy) {
			return false;
		}
	}

	/**
	 * 条件:<br>
	 * 访问次数>设定的最少访问次数<br>
	 * 访问次数>上层的访问次数均值<br>
	 * 剩余过期时间>=expireSeconds/2<br>
	 * <br>
	 * 上层溢出部分使用溢出策略处理<br>
	 * 上升部分的原层级如果是{@link org.supermoto.yamaha.R1.io.cache.distribution.DistributedCacher}，不会在该层级中删除,因为像redis这样的分布式缓存的存储容量很大<br>
	 * 
	 * @author Fangfang.Xu
	 */
	public class UpgradeGtMinAvgNotDeleteInDistributed implements GetOfUpgradeStrategy {
		private final long minUsedTimes;

		public UpgradeGtMinAvgNotDeleteInDistributed() {
			this(5);
		}

		public UpgradeGtMinAvgNotDeleteInDistributed(long minUsedTimes) {
			this.minUsedTimes = minUsedTimes;
		}
		
		@Override
		public <V> boolean upgrade(Level level, String key, V v, OutOfLimitStrategy outOfLimitStrategy) {
			//有上层
			if (level.getPre() != null) {
				Cacher cacher = level.getCacher();
				//需要基于 直接的 度量数据
				MetricsCacher metricsCacher = cacher.ofType(MetricsCacher.class);
				
				if(metricsCacher != null) {
					Cacher preCacher = level.getPre().getCacher();
					MetricsCacher preMetricsCacher = preCacher.ofType(MetricsCacher.class);
					
					final long usedTimes = metricsCacher.usedTimes(key);
					final long usedTimesAvg = preMetricsCacher != null ? preMetricsCacher.usedTimesAvg() : 0;
					final int expireSeconds = metricsCacher.expireSeconds(key);
					final int remainExpireSeconds = metricsCacher.remainExpireSeconds(key);

					if (usedTimes >= this.minUsedTimes && usedTimes > usedTimesAvg
							&& remainExpireSeconds >= expireSeconds / 2) {
						//Nullable
						List<Tuple3<String, Object, Integer>> removes = preCacher.set(key, v, expireSeconds);
						if(removes != null) {
							outOfLimitStrategy.set(level.getPre(), removes);
						}
						//不是分布式则在该层删除
						if(!cacher.instanceOf(DistributedCacher.class)) {
							cacher.remove(key);
						}
						
						return true;
					}
				}
			}
			return false;
		}
	}
	/**
	 * 上升部分在原来的层级中删除<br>
	 * @author Fangfang.Xu
	 */
	public class UpgradeGtMinAvgDeleteInCurrent extends UpgradeGtMinAvgNotDeleteInDistributed {

		public UpgradeGtMinAvgDeleteInCurrent(long minUsedTimes) {
			super(minUsedTimes);
		}
		
		@Override
		public <V> boolean upgrade(Level level, String key, V v, OutOfLimitStrategy outOfLimitStrategy) {
			boolean upgrade = super.upgrade(level, key, v, outOfLimitStrategy);
			if(upgrade) {
				Cacher cacher = level.getCacher();
				cacher.remove(key);
			}
			return upgrade;
		}
	}
}
