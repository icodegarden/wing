package org.supermoto.yamaha.R1.io.cache;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supermoto.yamaha.R1.io.cache.Cacher;



/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class PerformanceTests {
	private static Logger log = LoggerFactory.getLogger(PerformanceTests.class);
	
	protected Cacher cacher;
	
	@BeforeEach
	public void init() {
		cacher = getCacher();
	}

	protected abstract Cacher getCacher();
	
	@Test
	public void testPerformance() throws Exception {
		Random random = new Random();
		int loop = 100;
		long start = System.currentTimeMillis();
		for (int a = 0; a < loop; a++) {
			if(a % 100 == 0) {
				log.debug("testPerformance loop:" );
			}
			for (int i = 0; i < loop; i++) {
				final byte[] _KB = new byte[ random.nextInt(1024) + 1 ];
				cacher.getElseSupplier("{foo}."+i, ()->_KB, 30);
				
				if(i % 100 == 0) {
					log.debug("testPerformance loop:" + i);
				}
			}
		}
		long end = System.currentTimeMillis();
		log.info("performance "+this.getClass().getName()+" loop for "+loop+" used millis:"+(end-start));
	}
	
	class ByteArrayEq{
		boolean eq(byte[] a,byte[] b){
			if(a.length != b.length) {
				return false;
			}
			for(int i=0;i<a.length;i++) {
				if(a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
	}
}
