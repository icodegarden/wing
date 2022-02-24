package io.github.icodegarden.wing.protect;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * <p>
 * 应对穿透、击穿、雪崩
 * <p>
 * 击穿和雪崩区别： <br>
 * 相同：都是在高并发时获取不到缓存时高压DB的行为<br>
 * 不同：击穿是对相同的key而言，雪崩是大规模key，因此雪崩更严重，雪崩应从更多的角度措施去预防（永不过期、定时刷新、空值伪装...）
 * 
 * @author Fangfang.Xu
 *
 */
public interface Protector {

	/**
	 * 自己只需执行保护性动作，除此之外就把chain传递下去
	 * @param <V>
	 * @param chain
	 * @return
	 * @throws RejectedRequestException 拒绝方式的保护
	 */
	<V> V doProtector(ProtectorChain<V> chain) throws RejectedRequestException;
}
