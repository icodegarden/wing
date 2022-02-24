package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import java.io.Closeable;

import org.supermoto.yamaha.R1.io.cache.common.SyncFailedException;

/**
 * <p>
 * 当发生在 {@link org.supermoto.yamaha.R1.io.cache.distribution.DistributedCacher} 时,不需要进行同步,因为像redis这样的分布式缓存他自己相当于实现了同步
 * <p>
 * 执行步骤： 检查来源的应用名，与本实例的应用名不相同则不处理<br>
 * 检查来源的实例ID，与本实例相同则只需执行通讯；否则执行同步缓存操作<br>
 * @author Fangfang.Xu
 *
 */
public interface DistributionSyncStrategy extends Closeable {
	/**
	 * 如果已injectCacher，则false
	 * @param distributionSyncCacher
	 * @return
	 */
	boolean injectCacher(DistributionSyncCacher distributionSyncCacher);
	
	/**
	 * 同步发起者要做的事情
	 * @param <V>
	 * @param key
	 * @param v
	 * @param expireSeconds
	 * @throws SyncFailedException
	 */
	<V> void onSet(String key, V v, int expireSeconds) throws SyncFailedException;
	/**
	 * 同步发起者要做的事情
	 * @param key
	 * @throws SyncFailedException
	 */
	void onRemove(String key) throws SyncFailedException;
}
