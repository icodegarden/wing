package io.github.icodegarden.wing.redis;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Subscription;

import io.github.icodegarden.wing.common.Charsets;
import io.github.icodegarden.wing.redis.RedisExecutor.Unsubscribe;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class PoolRedisExecutor implements RedisExecutor {
	private static final Logger log = LoggerFactory.getLogger(PoolRedisExecutor.class);
	
	private JedisPool jedisPool;

	public PoolRedisExecutor(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public PoolRedisExecutor(JedisPoolConfig poolConfig, String host, int port, int timeout, String password,
			boolean ssl) {
		this.jedisPool = new JedisPool(poolConfig, host, port, timeout, password, ssl);
	}

	private <T> T execCommand(Command<T> co) {
		Jedis jedis = jedisPool.getResource();
		try {
			return co.exec(jedis);
		} finally {
			jedis.close();
		}
	}

	@FunctionalInterface
	private interface Command<T> {
		T exec(Jedis jedis);
	}

	@Override
	public byte[] get(byte[] key) {
		return execCommand(jedis -> jedis.get(key));
	}

	@Override
	public List<byte[]> mget(byte[]... keys) {
		return execCommand(jedis -> jedis.mget(keys));
	}

	@Override
	public String setex(byte[] key, int seconds, byte[] value) {
		return execCommand(jedis -> jedis.setex(key, seconds, value));
	}

	@Override
	public Object eval(byte[] script, int keyCount, byte[]... params) {
		return execCommand(jedis -> jedis.eval(script, keyCount, params));
	}

	@Override
	public Long del(byte[] key) {
		return execCommand(jedis -> jedis.del(key));
	}

	@Override
	public Long del(byte[]... keys) {
		return execCommand(jedis -> jedis.del(keys));
	}
	
	@Override
	public void subscribe(byte[] channel, BinaryJedisPubSub jedisPubSub,Consumer<Unsubscribe> unsubscribeReceiver) {
		execCommand(jedis -> {
			unsubscribeReceiver.accept(new Unsubscribe() {
				@Override
				public boolean isSubscribed() {
					return jedisPubSub.isSubscribed();
				}
				@Override
				public void unsubscribe(byte[]... channels) {
					jedisPubSub.unsubscribe(channels);
				}
				@Override
				public void unsubscribe() {
					jedisPubSub.unsubscribe();
					if(log.isInfoEnabled()) {
						log.info(this.getClass().getSimpleName() + " unsubscribe channel:{}",
								new String(channel, Charsets.UTF8));
					}
				}
			});
			
			jedis.subscribe(jedisPubSub, channel);
			return null;
		});
	}
	
	@Override
	public void publish(byte[] channel, byte[] message) {
		execCommand(jedis -> {
			jedis.publish(channel, message);
			return null;
		});
	}
}
