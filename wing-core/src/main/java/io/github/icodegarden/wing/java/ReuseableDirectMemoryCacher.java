package io.github.icodegarden.wing.java;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.commons.lang.tuple.Tuples;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import io.github.icodegarden.wing.common.ArgumentException;

/**
 * 空间可复用，自动清理
 * 
 * @author Fangfang.Xu
 *
 */
public class ReuseableDirectMemoryCacher extends DirectMemoryCacher {

	private static Logger log = LoggerFactory.getLogger(ReuseableDirectMemoryCacher.class);

	private final ScheduledThreadPoolExecutor clearIdlesThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool("clearIdles");

	private Map<String, Memory> map = new ConcurrentHashMap<String, Memory>();
	/**
	 * 猜测实际使用时每个idle容纳15K，共30M
	 */
	private int maxIdles = 2048;
	/**
	 * 最大一直处于idle的时间
	 */
	private int maxIdleSeconds = 60;
	/**
	 * 决定了重复利用的概率，越大概率越高，但是浪费的也多
	 */
	private float sizeFactor = 1.5F;
	private volatile NavigableMap<Integer, Memory> idles = new ConcurrentSkipListMap<>();

	private class Memory {
		private final ByteBuffer byteBuffer;
		private final long createdAt = System.currentTimeMillis();
		/**
		 * 什么时候开始idle的
		 */
		private long idledAt;
		private final AtomicLong usedTimes = new AtomicLong();

		public Memory(ByteBuffer byteBuffer) {
			this.byteBuffer = byteBuffer;
		}

		public void idle() {
			Memory pre = null;
			try {
				idledAt = System.currentTimeMillis();
				usedTimes.incrementAndGet();

				pre = ReuseableDirectMemoryCacher.this.idles.put(keyOfIdles(), this);// volatile
																						// 否则会有极小的概率同时得到pre，导致重复freeMemory报error
			} finally {
				if (pre != null) {
					freeMemory(pre.getByteBuffer());
				}
			}
		}

		private int keyOfIdles() {
			return byteBuffer.capacity();
		}

		public void free() {
			idles.remove(keyOfIdles());
			freeMemory(getByteBuffer());
		}

		public ByteBuffer getByteBuffer() {
			return byteBuffer;
		}

		public long getCreatedAt() {
			return createdAt;
		}

		public long getIdledSeconds() {
			return (System.currentTimeMillis() - idledAt) / 1000;
		}

		public long getUsedTimes() {
			return usedTimes.get();
		}
	}

	/**
	 * use JavaObjectSerializer ,JavaObjectDeserializer
	 */
	public ReuseableDirectMemoryCacher() {
		this(new JavaSerializer(), new JavaDeserializer());
	}

	public ReuseableDirectMemoryCacher(Serializer<?> serializer, Deserializer<?> deserializer) {
		super(serializer, deserializer);

		clearIdlesThreadPool.scheduleWithFixedDelay(() -> {
			try {
				clearIdles();
			} catch (Exception e) {
				log.error("clearIdles error", e);
			}
		}, 1, 5, TimeUnit.SECONDS);
	}

	void clearIdles() {
		/**
		 * 先清理idle时间超过maxIdleSeconds的<br>
		 * 接下来如果数量>maxIdles,清理超过部分， 生命(now-createdAt 越早越大)*使用次数 ,越小的越优先清理
		 */
		idles.entrySet().stream().filter(entry -> {
			Memory memory = entry.getValue();
			return memory.getIdledSeconds() > maxIdleSeconds;
		}).forEach(entry -> {
			Memory memory = entry.getValue();
			memory.free();
		});

		if (idles.size() > maxIdles) {
			long now = System.currentTimeMillis();
			idles.entrySet().stream().sorted((o1, o2) -> {
				Memory o1m = o1.getValue();
				Memory o2m = o2.getValue();

				long c1 = (now - o1m.getCreatedAt()) * o1m.getUsedTimes();
				long c2 = (now - o2m.getCreatedAt()) * o2m.getUsedTimes();
				return c1 <= c2 ? -1 : 1;
			}).limit(idles.size() - maxIdles).forEach(entry -> {
				Memory memory = entry.getValue();
				memory.free();
			});
		}
	}

	public int idleSize() {
		return idles.size();
	}

	/**
	 * 
	 * @return byte of int
	 */
	public int idleCapacity() {
		return idles.values().stream().mapToInt(memory -> memory.getByteBuffer().capacity()).sum();
	}

	public Collection<Memory> getIdles() {
		return idles.values();
	}

	public void setMaxIdles(int maxIdles) {
		this.maxIdles = maxIdles;
	}

	public void setMaxIdleSeconds(int maxIdleSeconds) {
		this.maxIdleSeconds = maxIdleSeconds;
	}

	/**
	 * 
	 * @param sizeFactor sizeFactor must gte 1.0
	 */
	public void setSizeFactor(float sizeFactor) {
		if (sizeFactor < 1) {
			throw new ArgumentException("sizeFactor must gte 1.0");
		}
		this.sizeFactor = sizeFactor;
	}

	private Memory allocateMemory(int requestSize) {
		Memory memory = null;
		Entry<Integer, Memory> entry = idles.ceilingEntry(requestSize);
		if (entry != null) {
			memory = idles.remove(entry.getKey());
			if (memory != null && log.isDebugEnabled()) {
				log.debug("allocate buffer from idles,requestSize:{},getted capacity:{}", requestSize,
						memory.getByteBuffer().capacity());
			}
		}

		if (memory == null) {
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) (requestSize * sizeFactor));
			if (log.isDebugEnabled()) {
				log.debug("allocate buffer from new allocate,requestSize:{},getted capacity:{}", requestSize,
						byteBuffer.capacity());
			}
			memory = new Memory(byteBuffer);
		}
		memory.getByteBuffer().clear();
		memory.getByteBuffer().limit(requestSize);
		return memory;
	}

	@Override
	public Integer spaceSize(String key) {
		Memory memory = map.get(key);
		if (memory == null) {
			return null;
		}
		return memory.getByteBuffer().limit();
	}

	@Override
	public <V> V get(String key) {
		Memory memory = map.get(key);
		if (memory == null) {
			return null;
		}
		return readObject(memory.getByteBuffer());
	}

	@Override
	public <V> List<Tuple3<String, Object, Integer>> set(String key, V v, int expireSeconds) {
		byte[] bs = seriaObject(v);
		Memory memory = allocateMemory(bs.length);
		ByteBuffer byteBuffer = memory.getByteBuffer();
		byteBuffer.put(bs);
		Memory pre = map.put(key, memory);
		if (pre != null) {
			pre.idle();
		}
		return null;
	}

	@Override
	public <V> Tuple3<String, V, Integer> remove(String key) {
		Memory memory = map.remove(key);
		if (memory == null) {
			return null;
		}
		ByteBuffer byteBuffer = memory.getByteBuffer();
		int limit = byteBuffer.limit();
		V remove = readObject(byteBuffer);

		memory.idle();

		return Tuples.of(key, remove, 0, limit);
	}
}
