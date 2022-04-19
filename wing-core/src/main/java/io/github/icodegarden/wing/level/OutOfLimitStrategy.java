package io.github.icodegarden.wing.level;

import java.util.List;

import io.github.icodegarden.commons.lang.tuple.Tuple3;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface OutOfLimitStrategy {

	/**
	 * 
	 * @param <V>
	 * @param level   对应层级
	 * @param removes 溢出部分
	 * @return 返回丢弃部分
	 */
	<V> List<Tuple3<String, Object, Integer>> set(Level level, List<Tuple3<String, Object, Integer>> removes);

	class ToNextLevel implements OutOfLimitStrategy {

		@Override
		public <V> List<Tuple3<String, Object, Integer>> set(Level level,
				List<Tuple3<String, Object, Integer>> removes) {
			while (removes != null && level.getNext() != null) {
				level = level.getNext();
				removes = level.getCacher().set(removes);
			}
			return removes;// 未能再存部分
		}
	}

	class Drop implements OutOfLimitStrategy {

		@Override
		public <V> List<Tuple3<String, Object, Integer>> set(Level level,
				List<Tuple3<String, Object, Integer>> removes) {
			return removes;// 丢弃
		}
	}
}
