package io.github.icodegarden.wing.java;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
public class DefaultDirectMemoryCacher extends DirectMemoryCacher {

	private Map<String, ByteBuffer> map = new ConcurrentHashMap<String, ByteBuffer>();

	/**
	 * use JavaObjectSerializer ,JavaObjectDeserializer
	 */
	public DefaultDirectMemoryCacher() {
		this(new JavaSerializer(), new JavaDeserializer());
	}

	public DefaultDirectMemoryCacher(Serializer<?> serializer, Deserializer<?> deserializer) {
		super(serializer, deserializer);
	}
	
	@Override
	public Integer spaceSize(String key) {
		ByteBuffer byteBuffer = map.get(key);
		if (byteBuffer == null) {
			return null;
		}
		return byteBuffer.limit();
	}

	@Override
	public <V> V get(String key) {
		ByteBuffer byteBuffer = map.get(key);
		if (byteBuffer == null) {
			return null;
		}
		return readObject(byteBuffer);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		byte[] bs = seriaObject(v);
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bs.length);
		byteBuffer.put(bs);
		ByteBuffer pre = map.put(key, byteBuffer);
		if (pre != null) {
			freeMemory(pre);
		}
		return null;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		ByteBuffer byteBuffer = map.remove(key);
		if (byteBuffer == null) {
			return null;
		}
		int limit = byteBuffer.limit();
		V remove = readObject(byteBuffer);

		freeMemory(byteBuffer);

		return Tuples.of(key, remove, 0, limit);
	}
	
}
