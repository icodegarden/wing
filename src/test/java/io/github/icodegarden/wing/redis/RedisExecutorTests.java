package io.github.icodegarden.wing.redis;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class RedisExecutorTests {

	protected abstract RedisExecutor newInstance();

	@Test
	public void testsubscribe() throws Exception {
//		RedisExecutor redisExecutor = newInstance();
//
//		BinaryJedisPubSub jedisPubSub = new BinaryJedisPubSub() {
//			@Override
//			public void onMessage(byte[] channel, byte[] message) {
//				System.out.println(new String(message,Charsets.UTF8));
//			}
//		};
//
//		redisExecutor.subscribe("io.cache.sync".getBytes(Charsets.UTF8), jedisPubSub, unsub -> {
//			new Thread() {
//				public void run() {
//					for(;;) {
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//						}
//						System.out.println(jedisPubSub.isSubscribed());
//					}
//				}
//			}.start();
//		});
	}
}
