package io.github.icodegarden.wing.redis;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.util.CollectionUtils;
import io.github.icodegarden.commons.redis.ClusterRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.TemplateRedisExecutor;
import io.github.icodegarden.wing.distribution.DistributedCacher;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings("rawtypes")
public class RedisCacher implements DistributedCacher {

	/**
	 * lua的数组索引从1开始<br>
	 * #KEYS是要操作的reids key的数量<br>
	 * redis eval传参位子对应：keycount->#KEYS，params首先是对应数量的keys，后面再跟values<br>
	 * 例如eval(SETBATCH_SCRIPT, 3, key1的byte[],key2的byte[],key3的byte[],expire1的byte[],expire2的byte[],expire3的byte[],v1的byte[],v2的byte[],v3的byte[]);
	 */
	private static final byte[] SETBATCH_SCRIPT = "for i=1,#KEYS do redis.call('setex',KEYS[i],ARGV[i],ARGV[#KEYS+i]) end;"
			.getBytes(Charset.forName("utf-8"));

	private final RedisExecutor redisExecutor;
	private final Serializer serializer;
	private final Deserializer deserializer;

	private RedisCacher(RedisExecutor redisExecutor, Serializer<?> serializer, Deserializer<?> deserializer) {
		this.redisExecutor = redisExecutor;
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	/**
	 * use java Serializer,Deserializer
	 * 
	 * @param jc
	 * @return
	 */
	public static RedisCacher jedisCluster(JedisCluster jc) {
		return jedisCluster(jc, new JavaSerializer(), new JavaDeserializer());
	}

	public static RedisCacher jedisCluster(JedisCluster jc, Serializer<?> serializer, Deserializer<?> deserializer) {
		return new RedisCacher(new ClusterRedisExecutor(jc), serializer, deserializer);
	}

	/**
	 * use java Serializer,Deserializer
	 * 
	 * @param jedisPool
	 * @return
	 */
	public static RedisCacher jedisPool(JedisPool jedisPool) {
		return jedisPool(jedisPool, new JavaSerializer(), new JavaDeserializer());
	}

	public static RedisCacher jedisPool(JedisPool jedisPool, Serializer<?> serializer, Deserializer<?> deserializer) {
		return new RedisCacher(new PoolRedisExecutor(jedisPool), serializer, deserializer);
	}

	public static RedisCacher redisTemplate(RedisTemplate redisTemplate) {
		return redisTemplate(redisTemplate, new JavaSerializer(), new JavaDeserializer());
	}

	public static RedisCacher redisTemplate(RedisTemplate redisTemplate, Serializer<?> serializer,
			Deserializer<?> deserializer) {
		return new RedisCacher(new TemplateRedisExecutor(redisTemplate), serializer, deserializer);
	}

	@Override
	public <V> V get(String key) {
		byte[] bs = redisExecutor.get(key.getBytes());
		if (bs == null) {
			return null;
		}
		return (V) deserializer.deserialize(bs);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		byte[][] keyBytesArray = CollectionUtils.toBytesArray(keys);
		List<byte[]> results = redisExecutor.mget(keyBytesArray);

		HashMap<String, V> ret = new HashMap<String, V>(results.size(), 1);
		Iterator<String> keyIt = keys.iterator();
		Iterator<byte[]> valueIt = results.iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			byte[] value = valueIt.next();
			ret.put(key, value != null ? (V) deserializer.deserialize(value) : null);
		}
		return ret;
	}

	/**
	 * expireSeconds如果不大于0，则认为已过期，不需要缓存
	 */
	@Override
	public <V> List<Tuple3<String, Object, Integer>> doSet(String key, V v, int expireSeconds) {
		if (expireSeconds > 0) {
			redisExecutor.setex(key.getBytes(), expireSeconds, serializer.serialize(v));
		}
		return null;
	}

	/**
	 * expireSeconds如果不大于0，则认为已过期，不需要缓存
	 */
	@Override
	public <V> List<Tuple3<String, Object, Integer>> doSet(List<Tuple3<String, V, Integer>> kvts) {
		List<Tuple3<String, V, Integer>> filtered = kvts.stream().filter(kvt -> kvt.getT3() > 0)
				.collect(Collectors.toList());
		if (!filtered.isEmpty()) {
			byte[][] params = new byte[filtered.size() * 3][];// 3倍的原因是每个都有key,v,expire
			for (int i = 0; i < filtered.size(); i++) {
				Tuple3<String, V, Integer> kvt = filtered.get(i);

				params[i] = kvt.getT1().getBytes();// key
				params[filtered.size() + i] = kvt.getT3().toString().getBytes();// 隐性*1，expire
				params[filtered.size() * 2 + i] = serializer.serialize(kvt.getT2());// *2 value
			}
			//eval(SETBATCH_SCRIPT, 3, key1的byte[],key2的byte[],key3的byte[],expire1的byte[],expire2的byte[],expire3的byte[],v1的byte[],v2的byte[],v3的byte[]);
			redisExecutor.eval(SETBATCH_SCRIPT, filtered.size(), params);// 这种性能损失最少，避免了jedis SDK的内部再次封装
		}
		return null;
	}

	@Override
	public <V> Tuple3<String, V, Integer> doRemove(String key) {
		redisExecutor.del(key.getBytes());// IMPT 要使用bytes与set时一致
		return null;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> doRemove(Collection<String> keys) {
		byte[][] keyBytesArray = CollectionUtils.toBytesArray(keys);
		redisExecutor.del(keyBytesArray);
		return null;
	}
}
