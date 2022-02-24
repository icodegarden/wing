package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supermoto.yamaha.R1.io.cache.Cacher;
import io.github.icodegarden.commons.lang.tuple.Tuple3;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DistributionSyncCacher implements Cacher {
	private static final Logger log = LoggerFactory.getLogger(DistributionSyncCacher.class);

	private boolean syncOnSet = true;
	private boolean syncOnRemove = true;
	private final String applicationName;
	private final String applicationInstanceId;
	private final Cacher cacher;
	private final DistributionSyncStrategy distributionSyncStrategy;

	public DistributionSyncCacher(String applicationName, Cacher cacher,
			DistributionSyncStrategy distributionSyncStrategy) {
		this.applicationName = applicationName;
		this.cacher = cacher;
		this.distributionSyncStrategy = distributionSyncStrategy;
		this.applicationInstanceId = Math.abs(UUID.randomUUID().toString().hashCode()) + "";

		distributionSyncStrategy.injectCacher(this);
	}

	/**
	 * set缓存时是否需要同步
	 * 
	 * @param syncOnSet 默认true
	 */
	public void syncOnSet(boolean syncOnSet) {
		this.syncOnSet = syncOnSet;
	}
	/**
	 * remove缓存时是否需要同步
	 * 
	 * @param syncOnRemove 默认true
	 */
	public void syncOnRemove(boolean syncOnRemove) {
		this.syncOnRemove = syncOnRemove;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getApplicationInstanceId() {
		return applicationInstanceId;
	}

	public Cacher getCacher() {
		return cacher;
	}

	public String toStringOfApplication() {
		return "[applicationName=" + applicationName + ", applicationInstanceId=" + applicationInstanceId + "]";
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
		List<Tuple3<String, Object, Integer>> removes = cacher.set(key, v, expireSeconds);
		if (syncOnSet) {
			try {
				distributionSyncStrategy.onSet(key, v, expireSeconds);
			} catch (Exception e) {
				log.error("ex on distribution sync onSet", e);
			}
		}
		return removes;
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, Object, Integer>> removes = cacher.set(kvts);
		if (syncOnSet) {
			kvts.parallelStream().forEach(kvt -> {
				try {
					distributionSyncStrategy.onSet(kvt.getT1(), kvt.getT2(), kvt.getT3());
				} catch (Exception e) {
					log.error("ex on distribution sync onSet", e);
				}
			});
		}
		return removes;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		Tuple3<String, V, Integer> remove = cacher.remove(key);
		if (syncOnRemove) {
			try {
				distributionSyncStrategy.onRemove(key);
			} catch (Exception e) {
				log.error("ex on distribution sync onRemove", e);
			}
		}
		return remove;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		List<Tuple3<String, V, Integer>> removes = cacher.remove(keys);
		if (syncOnRemove) {
			keys.parallelStream().forEach(key -> {
				try {
					distributionSyncStrategy.onRemove(key);
				} catch (Exception e) {
					log.error("ex on distribution sync onRemove", e);
				}
			});
		}
		return removes;
	}
}
