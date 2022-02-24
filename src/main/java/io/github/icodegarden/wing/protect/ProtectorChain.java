package io.github.icodegarden.wing.protect;

import java.util.List;
import java.util.function.Supplier;

import io.github.icodegarden.wing.Cacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ProtectorChain<V> {

	String key();

	V fromCache();

	V doProtector();

	class Default<V> implements ProtectorChain<V> {
		private int index = 0;
		private final List<Protector> protectors;

		private final String key;
		private final Cacher cacher;
		private final Supplier<V> supplier;
		private final int expireSeconds;

		public Default(Cacher cacher, String key, Supplier<V> supplier, int expireSeconds, List<Protector> protectors) {
			this.cacher = cacher;
			this.key = key;
			this.supplier = supplier;
			this.expireSeconds = expireSeconds;
			this.protectors = protectors;
		}

		@Override
		public String key() {
			return key;
		}

		@Override
		public V fromCache() {
			return cacher.get(key);
		}

		@Override
		public V doProtector() {
			if (index < protectors.size()) {
				Protector protector = protectors.get(index++);
				return protector.doProtector(this);
			} else if (index++ == protectors.size()) {// 越界时表示所有的protectors都已执行过
				return cacher.fromSupplier(key, supplier, expireSeconds);
			} else {
				throw new IndexOutOfBoundsException();
			}
		}
	}
}
