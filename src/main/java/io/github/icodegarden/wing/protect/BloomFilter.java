package io.github.icodegarden.wing.protect;

import java.util.BitSet;
import java.util.Collection;
import java.util.function.Predicate;

import io.github.icodegarden.wing.common.RejectedRequestException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class BloomFilter extends ShouldFilter {

	private static final int DEFAULT_SIZE = 1 << 24;

	private final int bitSize;

	private final BitSet bits;

	private final Hasher[] hashers;
	/**
	 * 使用默认bitSize,使用java string的hash算法
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public BloomFilter(int countOfHasher, Predicate<String> shouldFilter) {
		this(DEFAULT_SIZE, countOfHasher, shouldFilter);
	}
	/**
	 * 使用java string的hash算法
	 * @param bitSize
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public BloomFilter(int bitSize, int countOfHasher, Predicate<String> shouldFilter) {
		this(bitSize, new JavaStringHasher[countOfHasher], shouldFilter);

		int seed = 31 << (countOfHasher / 2);
		for (int i = 0; i < countOfHasher; i++) {
			hashers[i] = new JavaStringHasher(seed);
			seed = seed >> 1;
		}
	}
	/**
	 * 使用默认bitSize
	 * @param hashers
	 * @param shouldFilter
	 */
	public BloomFilter(Hasher[] hashers, Predicate<String> shouldFilter) {
		this(DEFAULT_SIZE, hashers, shouldFilter);
	}

	public BloomFilter(int bitSize, Hasher[] hashers, Predicate<String> shouldFilter) {
		super(shouldFilter);
		this.bitSize = bitSize;
		bits = new BitSet(bitSize);
		this.hashers = hashers;
	}

	public void add(String value) {
		for (Hasher f : hashers) {
			bits.set(f.hash(value) & (bitSize - 1));
		}
	}
	
	public void add(Collection<String> values) {
		values.forEach(v->{
			add(v);
		});
	}
	
	@Override
	protected void shouldDoFilter(String key) throws RejectedRequestException {
		if(!contains(key)) {
			throw new RejectedRequestException(this, "request key:" + key + " reject by bloom filter");
		}
	}

	boolean contains(String value) {
		if (value == null) {
			return false;
		}
		boolean ret = true;
		for (Hasher f : hashers) {
			if (ret) {
				ret = ret & bits.get(f.hash(value) & (bitSize - 1));
			}
		}
		return ret;
	}
	/**
	 * java 的string hash算法+seed
	 * @author Fangfang.Xu
	 *
	 */
	class JavaStringHasher implements Hasher {
		private final int seed;

		public JavaStringHasher(int seed) {
			this.seed = seed;
		}

		@Override
		public int hash(String value) {
			int hash = 0;

			int h = hash;
			if (h == 0 && value.length() > 0) {
				for (int i = 0; i < value.length(); i++) {
					h = seed * h + value.charAt(i);
				}
				hash = h;
			}
			return h;
		}
	}

}