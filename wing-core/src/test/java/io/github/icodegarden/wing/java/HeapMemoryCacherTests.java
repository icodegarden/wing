package io.github.icodegarden.wing.java;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.CacherTests;
import io.github.icodegarden.wing.java.HeapMemoryCacher;

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
