package org.supermoto.yamaha.R1.io.cache;

/**
 * 可计算缓存大小占用
 * @author Fangfang.Xu
 *
 */
public interface SpaceCalcableCacher extends Cacher, SpaceCalcable {

	/**
	 * 
	 * @param key
	 * @return Nullable, key对应的value的space大小，如果value不存在返回null
	 */
	Integer spaceSize(String key);
}
