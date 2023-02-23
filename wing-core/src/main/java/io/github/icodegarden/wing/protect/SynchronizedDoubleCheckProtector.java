package io.github.icodegarden.wing.protect;

import io.github.icodegarden.wing.common.RejectedCacheException;

/**
 * 
 * <p>
 * 同步并再次从cache中检查是否存在，不存在再loadDB
 * <p>
 * 优点：机制简单，相同key在高并发时只1次loadDB
 * <p>
 * 缺点：第一个进行双重检查的行为是徒劳； 由于所有key都进行同步，吞吐量受影响<br>
 * 
 * 
 * @author Fangfang.Xu
 *
 */
public class SynchronizedDoubleCheckProtector implements Protector {

	protected Object synchronizer(String key) {
		return this;
	}

	@Override
	public <V> V doProtector(ProtectorChain<V> chain) throws RejectedCacheException {
		String key = chain.key();
		synchronized (synchronizer(key)) {
			V v = chain.fromCache();
			if (v == null) {
				v = chain.doProtector();
			}
			return v;
		}
	}
}
