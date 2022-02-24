package io.github.icodegarden.wing.java;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.wing.SpaceCalcableCacher;
import io.github.icodegarden.wing.common.EnvException;
import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;

/**
 * 自动回收：<br>
 * 1在JVM FULL GC时回收失去引用的部分<br>
 * 2在{@link java.nio.ByteBuffer#allocateDirect} -> new DirectByteBuffer ->
 * {@link java.nio.Bits#reserveMemory} 发现直接内存不足时，会触发System.gc回收失去引用部分<br>
 * 自动回收原理：创建DirectByteBuffer时产生cleaner，回收时调用clean()<br>
 * 
 * 手动回收：<br>
 * 1使用System.gc;<br>
 * 2手动调用clean（不适用于JDK9及以上,由于引入了模块化特性无法访问到）<br>
 * java.lang.reflect.InaccessibleObjectException: Unable to make field private
 * static final jdk.internal.misc.Unsafe jdk.internal.misc.Unsafe.theUnsafe
 * accessible: module java.base does not "opens jdk.internal.misc" to unnamed
 * module @e50a6f6<br>
 * 
 * java.lang.IllegalAccessException: class xxx cannot access class
 * jdk.internal.ref.Cleaner (in module java.base) because module java.base does
 * not export jdk.internal.ref to unnamed module @14ec4505<br>
 * 
 * 设置JVM最大直接内存 -XX:MaxDirectMemorySize=500M<br>
 * 
 * 获取溢出时报 java.lang.OutOfMemoryError: Direct buffer memory, JDK9以上
 * java.lang.OutOfMemoryError: Cannot reserve 5242880 bytes of direct buffer
 * memory (allocated: 5251465, limit: 10485760)<br>
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
@SuppressWarnings("rawtypes")
public abstract class DirectMemoryCacher implements SpaceCalcableCacher {

	private static Logger log = LoggerFactory.getLogger(DirectMemoryCacher.class);

	private static final Method METHOD_OF_CLEANER = cleanerMethod();
	private static Method METHOD_OF_CLEAN;
	
	private static Method cleanerMethod() {
		final String claName = "sun.nio.ch.DirectBuffer";
		String javaVersion = System.getProperty("java.specification.version");
		if (log.isDebugEnabled()) {
			log.debug("java.specification.version:{}", javaVersion);
		}
		try {
			if (Double.parseDouble(javaVersion) < 9) {
				Class<?> forName = Class.forName(claName);
				return forName.getDeclaredMethod("cleaner", null);
			}
			return null;
		} catch (NumberFormatException | ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new EnvException("get " + claName + " cleaner ex", e);
		}
	}

	/**
	 * test freeMemory
	 */
	static {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1);
		freeMemory(byteBuffer);
	}

	static boolean freeMemory(ByteBuffer byteBuffer) {
		if (!byteBuffer.isDirect()) {
			return false;
		}
		if (METHOD_OF_CLEANER == null) {
			return false;
		}
		try {
			// jdk.internal.ref.Cleaner
			// sun.misc.Cleaner
			
//			直接使用 sun.misc.Cleaner 不能被编译
//			sun.misc.Cleaner cleaner = (sun.misc.Cleaner) METHOD_OF_CLEANER.invoke(byteBuffer, null);
//			cleaner.clean();
			
			Object cleaner = METHOD_OF_CLEANER.invoke(byteBuffer, null);
			if(METHOD_OF_CLEAN == null) {
				METHOD_OF_CLEAN = cleaner.getClass().getDeclaredMethod("clean", null);
			}
			METHOD_OF_CLEAN.invoke(cleaner, null);
			
			if (log.isDebugEnabled()) {
				log.debug("Direct Memory size:{} was freed", byteBuffer.capacity());
			}
		} catch (Exception e) {
			throw new EnvException("freeMemory use cleaner ex", e);
		}
		return true;
	}

	private Serializer serializer;
	private Deserializer deserializer;

	/**
	 * use JavaObjectSerializer ,JavaObjectDeserializer
	 */
	public DirectMemoryCacher() {
		this(new JavaSerializer(), new JavaDeserializer());
	}

	public DirectMemoryCacher(Serializer<?> serializer, Deserializer<?> deserializer) {
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	byte[] seriaObject(Object obj) {
		return serializer.serialize(obj);
	}

	Object deseriaObject(byte[] bytes) {
		return deserializer.deserialize(bytes);
	}

	@Override
	public <V> Map<String, V> get(Collection<String> keys) {
		Map<String, V> result = new HashMap<String, V>(keys.size(), 1);
		keys.forEach(key -> {
			V v = get(key);
			result.put(key, v);
		});
		return result;
	}

	/**
	 * 
	 * @param <V>
	 * @param byteBuffer NOT NULL
	 * @return
	 */
	<V> V readObject(ByteBuffer byteBuffer) {
		byte[] bs = new byte[byteBuffer.limit()];

		/**
		 * byteBuffer.flip(); byteBuffer.get(bs);有并发安全问题 
		 */

		for (int index = 0; index < bs.length; index++) {
			bs[index] = byteBuffer.get(index);
		}

		return (V) deseriaObject(bs);
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(List<Tuple3<String, V, Integer>> kvts) {
		kvts.forEach(kvt -> {
			set(kvt.getT1(), kvt.getT2(), kvt.getT3());
		});
		return null;
	}

	@Override
	public <V> List<Tuple3<String, V, Integer>> remove(Collection<String> keys) {
		List<Tuple3<String, V, Integer>> collect = keys.stream().map(key -> {
			Tuple3<String, V, Integer> remove = remove(key);
			return remove;
		}).filter(i -> i != null).collect(Collectors.toList());
		return collect.isEmpty() ? null : collect;// 空的时候返回null
	}
	
	@Override
	public int spaceSizeCalc(Object v) {
		return seriaObject(v).length;
	}
}
