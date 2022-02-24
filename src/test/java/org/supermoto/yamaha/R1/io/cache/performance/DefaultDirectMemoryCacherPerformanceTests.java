package org.supermoto.yamaha.R1.io.cache.performance;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.java.DefaultDirectMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultDirectMemoryCacherPerformanceTests extends PerformanceTests {
	
	@Override
	protected String name() {
		return "DefaultDirectMemoryCacher";
	}
	
	@Override
	protected Cacher newCacher() {
		return new DefaultDirectMemoryCacher();
	}
	
}
