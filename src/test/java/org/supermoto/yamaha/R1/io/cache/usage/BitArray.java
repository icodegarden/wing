package org.supermoto.yamaha.R1.io.cache.usage;
class BitArray{
		final boolean[] bits;
		BitArray(int numBits){
			bits = new boolean[numBits];
		}
		/**
		 * from 0 to (numBits - 1)
		 * @param bitIndex
		 */
		public void set(int bitIndex) {
			if(bitIndex > bits.length) {
				throw new IndexOutOfBoundsException("max bitIndex is "+bits.length+", bitIndex:"+bitIndex);
			}
			bits[bitIndex] = true;
		}
		public boolean get(int bitIndex) {
			if (bitIndex < 0)
	            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
			if(bitIndex > bits.length) {
				throw new IndexOutOfBoundsException("max bitIndex is "+bits.length+", bitIndex:"+bitIndex);
			}
			return bits[bitIndex];
		}
	}