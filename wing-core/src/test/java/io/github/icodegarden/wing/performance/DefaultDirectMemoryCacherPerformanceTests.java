package io.github.icodegarden.wing.performance;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.java.DefaultDirectMemoryCacher;

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
