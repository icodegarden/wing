package io.github.icodegarden.wing.distribution.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.wing.common.SyncFailedException;
import io.github.icodegarden.wing.distribution.DistributedCacher;

/**
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractDistributionSyncStrategy implements DistributionSyncStrategy {
	private static final Logger log = LoggerFactory.getLogger(AbstractDistributionSyncStrategy.class);

	protected DistributionSyncCacher distributionSyncCacher;

	@Override
	public boolean injectCacher(DistributionSyncCacher distributionSyncCacher) {
		if (this.distributionSyncCacher == null) {
			this.distributionSyncCacher = distributionSyncCacher;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public <V> void onSet(String key, V v, int expireSeconds) throws SyncFailedException {
		OnSet onSet = new OnSet();
		onSet.setApplicationName(distributionSyncCacher.getApplicationName());
		onSet.setApplicationInstanceId(distributionSyncCacher.getApplicationInstanceId());
		onSet.setKey(key);
		onSet.setV(v);
		onSet.setExpireSeconds(expireSeconds);

		broadcast(onSet);
		if (log.isInfoEnabled()) {
			log.info("distribution sync onSet was broadcast, key:{}, {}", key,
					distributionSyncCacher.toStringOfApplication());
		}
	}

	@Override
	public void onRemove(String key) throws SyncFailedException {
		OnRemove onRemove = new OnRemove();
		onRemove.setApplicationName(distributionSyncCacher.getApplicationName());
		onRemove.setApplicationInstanceId(distributionSyncCacher.getApplicationInstanceId());
		onRemove.setKey(key);

		broadcast(onRemove);
		if (log.isInfoEnabled()) {
			log.info("distribution sync onRemove was broadcast, key:{}, {}", key,
					distributionSyncCacher.toStringOfApplication());
		}
	}

	protected abstract void broadcast(DistributionSyncDTO message) throws SyncFailedException;

	protected void receiveSync(DistributionSyncDTO distributionSyncDTO) {
		if (log.isInfoEnabled()) {
			log.info("distribution sync message was received, key:{}, {}", distributionSyncDTO.getKey(),
					distributionSyncCacher.toStringOfApplication());
		}
		if (!distributionSyncCacher.getApplicationName().equals(distributionSyncDTO.getApplicationName())) {
			return;
		}
		if (distributionSyncCacher.getApplicationInstanceId().equals(distributionSyncDTO.getApplicationInstanceId())) {
			return;
		} else {
			if (distributionSyncDTO instanceof OnSet) {
				OnSet onSet = ((OnSet) distributionSyncDTO);
				receiveSet(onSet.getKey(), onSet.getV(), onSet.getExpireSeconds());
			} else if (distributionSyncDTO instanceof OnRemove) {
				OnRemove onRemove = ((OnRemove) distributionSyncDTO);
				receiveRemove(onRemove.getKey());
			}
		}
	}
	/**
	 * 当分布式同步时，是{@link io.github.icodegarden.wing.distribution.DistributedCacher}的只需要发起者实例执行1次，接收方不需要执行
	 */
	private <V> void receiveSet(String key, V v, int expireSeconds) {
		DistributedCacher.shouldWriteOpThread(false);
		try {
			// 必须使用真实cacher，否则死循环broadcast
			distributionSyncCacher.getCacher().set(key, v, expireSeconds);
			if (log.isInfoEnabled()) {
				log.info("distribution sync oper set is done, key:{}, {}", key,
						distributionSyncCacher.toStringOfApplication());
			}
		} finally {
			DistributedCacher.shouldWriteOpThread(true);
		}
	}
	/**
	 * 当分布式同步时，是{@link io.github.icodegarden.wing.distribution.DistributedCacher}的只需要发起者实例执行1次，接收方不需要执行
	 */
	private <V> void receiveRemove(String key) {
		DistributedCacher.shouldWriteOpThread(false);
		try {
			// 必须使用真实cacher，否则死循环broadcast
			distributionSyncCacher.getCacher().remove(key);
			if (log.isInfoEnabled()) {
				log.info("distribution sync oper remove is done, key:{}, {}", key,
						distributionSyncCacher.toStringOfApplication());
			}
		} finally {
			DistributedCacher.shouldWriteOpThread(true);
		}
	}
}
