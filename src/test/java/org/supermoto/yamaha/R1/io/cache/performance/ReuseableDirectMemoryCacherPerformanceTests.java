package org.supermoto.yamaha.R1.io.cache.performance;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.java.ReuseableDirectMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ReuseableDirectMemoryCacherPerformanceTests extends PerformanceTests  {

	@Override
	protected String name() {
		return "ReuseableDirectMemoryCacher";
	}
	
	@Override
	protected Cacher newCacher() {
		return new ReuseableDirectMemoryCacher();
	}
}
