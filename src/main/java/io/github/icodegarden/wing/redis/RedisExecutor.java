package io.github.icodegarden.wing.redis;

import java.util.List;
import java.util.function.Consumer;

import redis.clients.jedis.BinaryJedisPubSub;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RedisExecutor {

	byte[] get(final byte[] key);

	List<byte[]> mget(final byte[]... keys);

	String setex(final byte[] key, final int seconds, final byte[] value);

	Object eval(final byte[] script, final int keyCount, final byte[]... params);

	Long del(final byte[] key);

	Long del(final byte[]... keys);
	/**
	 * 该动作是一直阻塞的，直到unsubscribe
	 * @param channel
	 * @param jedisPubSub
	 * @param unsubscribeReceiver
	 */
	void subscribe(byte[] channel,BinaryJedisPubSub jedisPubSub,Consumer<Unsubscribe> unsubscribeReceiver);
	
	void publish(byte[] channel,byte[] message);
	
	interface Unsubscribe{
		boolean isSubscribed();
		/**
		 * unsubscribe all 
		 */
		void unsubscribe();
		/**
		 * 不可以传(byte[])null,否则入参是[null]而不是null
		 * @param channels Notnull
		 */
		void unsubscribe(byte[]... channels);
	}
}
