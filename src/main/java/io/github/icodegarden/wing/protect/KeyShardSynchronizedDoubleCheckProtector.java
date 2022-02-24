package io.github.icodegarden.wing.protect;

/**
 * <p>
 * 以key作为分段锁
 * <p>
 * 注意场景中如果key数量巨大，容易占用较大的JVM运行时常量池
 * 
 * @author Fangfang.Xu
 *
 */
public class KeyShardSynchronizedDoubleCheckProtector extends SynchronizedDoubleCheckProtector {

	@Override
	protected Object synchronizer(String key) {
		return key.intern();
	}
}
