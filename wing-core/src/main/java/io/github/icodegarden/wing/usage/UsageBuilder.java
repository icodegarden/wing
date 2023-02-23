package io.github.icodegarden.wing.usage;

import java.util.Arrays;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.KeySizeLRUCacher;
import io.github.icodegarden.wing.SpaceSizeLRUCacher;
import io.github.icodegarden.wing.common.ArgumentCacheException;
import io.github.icodegarden.wing.expire.AutoExpireCacher;
import io.github.icodegarden.wing.java.HeapMemoryCacher;
import io.github.icodegarden.wing.java.ReuseableDirectMemoryCacher;
import io.github.icodegarden.wing.level.GetOfUpgradeStrategy;
import io.github.icodegarden.wing.level.LevelableCacher;
import io.github.icodegarden.wing.level.OutOfLimitStrategy;
import io.github.icodegarden.wing.level.SetOfFromStrategy;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;
import io.github.icodegarden.wing.metrics.SpaceMetricsCacher;
import io.github.icodegarden.wing.redis.RedisCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class UsageBuilder {

	private final static long _1MB = 1024 * 1024;

	/**
	 * 基于Heap,Direct, redis 作为L3
	 * 
	 * @return
	 */
	public static RedisOfL3BasedOnHeapAndDirectBuilder redisOfL3BasedOnHeapAndDirectBuilder() {
		return new RedisOfL3BasedOnHeapAndDirectBuilder();
	}

	/**
	 * 基于Heap,Direct, redis 作为L3
	 * 
	 * @author Fangfang.Xu
	 *
	 */
	public static class RedisOfL3BasedOnHeapAndDirectBuilder {
		private RedisCacher redisCacher;
		private Serializer<?> serializer = new JavaSerializer();
		private Deserializer<?> deserializer = new JavaDeserializer();
		private long maxKeySizeOfHeap = 2048;// 每个key预期15K
		private long maxBytesOfDirect = 100 * _1MB;
		/**
		 * SetOfFromStrategy从redis开始<br>
		 * 优点是在分布式下从redis开始设置，则集群实例间的缓存自然得到了同步，避免多实例时重复loadDB<br>
		 * 缺点是新进的缓存在获取时，从L1、L2查找不到，需要等待热度提升<br>
		 * GetOfUpgradeStrategy需要能向上提升，并不要删除分布式，以免集群实例间找不到缓存需要loadDB<br>
		 */
		private SetOfFromStrategy setOfFrom = new SetOfFromStrategy.Highest();
		private long upgradeMinUsedTimes = 5;

		private RedisOfL3BasedOnHeapAndDirectBuilder() {
		}

		public LevelableCacher build() {
			if (redisCacher == null) {
				throw new ArgumentCacheException("redisCacher must not null");
			}
			Cacher L1 = new KeySizeLRUCacher(new KeySizeMetricsCacher(new AutoExpireCacher(new HeapMemoryCacher())),
					maxKeySizeOfHeap);
			Cacher L2 = new AutoExpireCacher(new SpaceSizeLRUCacher(
					new SpaceMetricsCacher(new ReuseableDirectMemoryCacher(serializer, deserializer)),
					maxBytesOfDirect));
			
			Cacher L3 = new KeySizeMetricsCacher(redisCacher);//redis可以自己管理过期，不需要AutoExpireCacher
			
			return new LevelableCacher(Arrays.asList(L1, L2, L3), setOfFrom,
					new OutOfLimitStrategy.ToNextLevel(), new GetOfUpgradeStrategy.UpgradeGtMinAvgNotDeleteInDistributed(upgradeMinUsedTimes));
		}

		public RedisOfL3BasedOnHeapAndDirectBuilder redisCacher(RedisCacher redisCacher) {
			this.redisCacher = redisCacher;
			return this;
		}

		public RedisOfL3BasedOnHeapAndDirectBuilder serializer(Serializer<?> serializer) {
			this.serializer = serializer;
			return this;
		}

		public RedisOfL3BasedOnHeapAndDirectBuilder deserializer(Deserializer<?> deserializer) {
			this.deserializer = deserializer;
			return this;
		}

		public RedisOfL3BasedOnHeapAndDirectBuilder maxKeySizeOfHeap(long maxKeySizeOfHeap) {
			this.maxKeySizeOfHeap = maxKeySizeOfHeap;
			return this;
		}

		public RedisOfL3BasedOnHeapAndDirectBuilder maxBytesOfDirect(long maxBytesOfDirect) {
			this.maxBytesOfDirect = maxBytesOfDirect;
			return this;
		}
		public RedisOfL3BasedOnHeapAndDirectBuilder setOfFrom(SetOfFromStrategy setOfFrom) {
			this.setOfFrom = setOfFrom;
			return this;
		}
		public RedisOfL3BasedOnHeapAndDirectBuilder upgradeMinUsedTimes(long upgradeMinUsedTimes) {
			this.upgradeMinUsedTimes = upgradeMinUsedTimes;
			return this;
		}
		
	}

}
