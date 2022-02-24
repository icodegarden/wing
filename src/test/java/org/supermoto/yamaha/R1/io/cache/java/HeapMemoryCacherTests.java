package org.supermoto.yamaha.R1.io.cache.java;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.CacherTests;
import org.supermoto.yamaha.R1.io.cache.java.HeapMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class HeapMemoryCacherTests extends CacherTests {

	HeapMemoryCacher heapMemoryCacher;

	@Override
	protected Cacher getCacher() {
		heapMemoryCacher = new HeapMemoryCacher();
		return heapMemoryCacher;
	}

}
