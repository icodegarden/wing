package io.github.icodegarden.wing;

import java.util.List;

import io.github.icodegarden.commons.lang.tuple.Tuple3;
import io.github.icodegarden.wing.common.ArgumentCacheException;
import io.github.icodegarden.wing.metrics.SpaceMetricsCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SpaceSizeLRUCacher extends CapacityLimitedCacher {

	protected final long maxBytes;
	private final SpaceMetricsCacher cacher;

	public SpaceSizeLRUCacher(SpaceMetricsCacher cacher, long maxBytes) {
		super(cacher);
		this.maxBytes = maxBytes;
		this.cacher = cacher;
	}

	@Override
	protected <V>List<Tuple3<String, Object, Integer>> removePreSet(List<Tuple3<String, V, Integer>> kvts) {
		long needSpace = kvts.stream().mapToLong(kvt -> cacher.spaceSize(kvt.getT2())).sum();
		if (maxBytes < needSpace) {
			//用户有义务设置足够大的maxBytes
			throw new ArgumentCacheException("maxBytes:" + maxBytes + " <(lt) request key needSpace:" + needSpace);
		}
		
		//已存在+请求 > 最大设置
		if(cacher.usedSpaceSize() + needSpace > maxBytes) {
			return lruOfAvg(kvts.size());
		}
		return null;
	}
	
}
