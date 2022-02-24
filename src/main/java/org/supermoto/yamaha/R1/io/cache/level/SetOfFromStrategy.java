package org.supermoto.yamaha.R1.io.cache.level;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface SetOfFromStrategy {
	
	Level select(Level L1);
	
	class Lowest implements SetOfFromStrategy {
		
		@Override
		public Level select(Level L1) {
			return L1;
		}
	}
	
	class Highest implements SetOfFromStrategy {
		
		@Override
		public Level select(Level L1) {
			Level l = L1;
			while (l.getNext() != null) {
				l = l.getNext();
			}
			return l;
		}
	}
}
