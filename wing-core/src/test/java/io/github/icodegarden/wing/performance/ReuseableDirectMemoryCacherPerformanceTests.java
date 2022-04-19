package io.github.icodegarden.wing.performance;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.java.ReuseableDirectMemoryCacher;

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
