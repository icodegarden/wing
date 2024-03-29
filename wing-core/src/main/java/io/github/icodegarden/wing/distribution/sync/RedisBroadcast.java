package io.github.icodegarden.wing.distribution.sync;

import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Serializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import io.github.icodegarden.commons.redis.ClusterRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor.Unsubscribe;
import io.github.icodegarden.commons.redis.TemplateRedisExecutor;
import io.github.icodegarden.wing.common.Charsets;
import io.github.icodegarden.wing.common.EnvCacheException;
import io.github.icodegarden.wing.common.SyncFailedCacheException;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings("rawtypes")
public class RedisBroadcast extends AbstractDistributionSyncStrategy {

	private static final Logger log = LoggerFactory.getLogger(RedisBroadcast.class);

	private final ScheduledThreadPoolExecutor scheduleCheckSubscribeThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool("Schedule-Check-Subscribe");

//	private static final byte[] SUB_CHANNEL = "io.cahce.sync".getBytes(Charsets.UTF8);

	private static final Serializer<Object> SERIALIZER = new Hessian2Serializer();
	private static final Deserializer<Object> DESERIALIZER = new Hessian2Deserializer();

	public static RedisBroadcast jedisCluster(JedisCluster jc) {
		return new RedisBroadcast(new ClusterRedisExecutor(jc));
	}

	public static RedisBroadcast jedisPool(JedisPool jedisPool) {
		return new RedisBroadcast(new PoolRedisExecutor(jedisPool));
	}

	public static RedisBroadcast redisTemplate(RedisTemplate redisTemplate) {
		return new RedisBroadcast(new TemplateRedisExecutor(redisTemplate));
	}

	private byte[] SUB_CHANNEL = "io.cahce.sync".getBytes(Charsets.UTF8);

	private long lastMessageMillis = System.currentTimeMillis();

//	private ReentrantLock lock = new ReentrantLock();
//	private Condition condition = lock.newCondition();

	private Unsubscribe unsubscribe;

	private final RedisExecutor redisExecutor;

	private RedisBroadcast(RedisExecutor redisExecutor) {
		this.redisExecutor = redisExecutor;
	}

	@Override
	public boolean injectCacher(DistributionSyncCacher distributionSyncCacher) {
		boolean inject = super.injectCacher(distributionSyncCacher);
		if (inject) {
			this.SUB_CHANNEL = ("io.cahce.sync." + distributionSyncCacher.getApplicationName()).getBytes(Charsets.UTF8);

			subBroadcast();
			/**
			 * 可能由于网络长时间中断（短时间的中断，redis sub 能够自动恢复），需要重连检查
			 */
			scheduleCheckSubscribeThreadPool.scheduleAtFixedRate(() -> {
				try {
					if (!unsubscribe.isSubscribed() || (System.currentTimeMillis() - lastMessageMillis) > 600 * 1000) {// 超过10分钟
						if (log.isInfoEnabled()) {
							log.info("channel {} was unsubscribed, restart subscribe",
									new String(SUB_CHANNEL, Charsets.UTF8));
						}
						unsubscribe.unsubscribe();
						subBroadcast();
					}
				} catch (Exception e) {
					log.error("schedule check channel {} whether subscribed error",
							new String(SUB_CHANNEL, Charsets.UTF8), e);
				}
			}, 10000, 3000, TimeUnit.MILLISECONDS);
		}
		return inject;
	}

	private void subBroadcast() throws EnvCacheException {
		BinaryJedisPubSub jedisPubSub = new BinaryJedisPubSub() {
			@Override
			public void onSubscribe(byte[] channel, int subscribedChannels) {
				log.info("channel {} was onSubscribe,subscribedChannels:{}", new String(channel, Charsets.UTF8),
						subscribedChannels);
//				lock.lock();
//				try{
//					condition.signal();
//				} finally {
//					lock.unlock();
//				}
			}

			@Override
			public void onUnsubscribe(byte[] channel, int subscribedChannels) {
				log.info("channel {} was onUnsubscribe,subscribedChannels:{}", new String(channel, Charsets.UTF8),
						subscribedChannels);
			}

			@Override
			public void onMessage(byte[] channel, byte[] message) {
				lastMessageMillis = System.currentTimeMillis();
				DistributionSyncDTO distributionSyncDTO = (DistributionSyncDTO) DESERIALIZER.deserialize(message);
				receiveSync(distributionSyncDTO);
			}
		};

		new Thread(RedisBroadcast.class.getSimpleName() + "-subscribe") {
			public void run() {
				redisExecutor.subscribe(SUB_CHANNEL, jedisPubSub, unsub -> {
					unsubscribe = unsub;
				});
			};
		}.start();

//		lock.lock();
//		try{
//			if (log.isInfoEnabled()) {
//				log.info("waiting for subscribe channel:" + new String(SUB_CHANNEL, Charsets.UTF8));
//			}
//			long waitMillis = 3000;
//			if (!condition.await(waitMillis, TimeUnit.MILLISECONDS)) {
//				throw new EnvException("subscribe channel:" + new String(SUB_CHANNEL, Charsets.UTF8) + " failed after "
//						+ waitMillis + " millis");
//			}
//		} catch (InterruptedException e) {
//		} finally {
//			lock.unlock();
//		}
	}

	@Override
	protected void broadcast(DistributionSyncDTO message) throws SyncFailedCacheException {
		byte[] bytes = SERIALIZER.serialize(message);
		try {
			redisExecutor.publish(SUB_CHANNEL, bytes);
		} catch (Exception e) {
			throw new SyncFailedCacheException("redis publish error", e);
		}
	}

	@Override
	public void close() throws IOException {
		if (unsubscribe != null) {
			unsubscribe.unsubscribe();
		}

		scheduleCheckSubscribeThreadPool.setRemoveOnCancelPolicy(true);
		scheduleCheckSubscribeThreadPool.shutdown();
	}

}
