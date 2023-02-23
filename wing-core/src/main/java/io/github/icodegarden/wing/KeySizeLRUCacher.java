package io.github.icodegarden.wing;

import java.util.List;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.wing.common.ArgumentCacheException;
import io.github.icodegarden.wing.metrics.KeySizeMetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class KeySizeLRUCacher extends CapacityLimitedCacher {

	protected final long maxKeySize;
	private final KeySizeMetricsCacher cacher;

	public KeySizeLRUCacher(KeySizeMetricsCacher cacher, long maxKeySize) {
		super(cacher);
		this.maxKeySize = maxKeySize;
		this.cacher = cacher;
	}

	@Override
	protected <V>List<Tuple3<String, Object, Integer>> removePreSet(List<Tuple3<String, V, Integer>> kvts) {
		if (maxKeySize < kvts.size()) {
			//用户有义务设置足够大的maxKeySize
			throw new ArgumentCacheException("maxKeySize:" + maxKeySize + " <(lt) request key size:" + kvts.size());
		}
		//已存在+请求数 > 最大设置
		if (cacher.keySize() + kvts.size() > maxKeySize) {
			return lruOfAvg(kvts.size());
		}
		return null;
	}
	
}
