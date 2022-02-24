package org.supermoto.yamaha.R1.io.cache.protect;

/**
 * <p>
 * 以key的hash作为分段锁
 * 
 * @author Fangfang.Xu
 *
 */
public class KeyHashShardSynchronizedDoubleCheckProtector extends SynchronizedDoubleCheckProtector {

	private static final int DEFAULT_SHARD = 16834;

	private final Object[] tab;

	public KeyHashShardSynchronizedDoubleCheckProtector() {
		this(DEFAULT_SHARD);
	}

	/**
	 * 
	 * @param shard 分段数
	 */
	public KeyHashShardSynchronizedDoubleCheckProtector(int shard) {
		tab = new Object[shard];
		for (int i = 0; i < tab.length; i++) {
			tab[i] = new Object();
		}
	}

	@Override
	protected Object synchronizer(String key) {
		return getNode(tab, hash(key));
	}

	/**
	 * 参考 hashmap
	 * 
	 * @param key
	 * @return
	 */
	private final int hash(Object key) {
		int h;
		return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
	}

	/**
	 * 参考 hashmap, 实际效果与hash取模相似
	 * 
	 * @param tab
	 * @param hash
	 * @return
	 */
	private final Object getNode(Object[] tab, int hash) {
		return tab[(tab.length - 1) & hash];// -1 防越界
	}
}
