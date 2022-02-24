package io.github.icodegarden.wing.distribution.sync;

import io.github.icodegarden.wing.distribution.sync.AbstractDistributionSyncStrategy;
import io.github.icodegarden.wing.distribution.sync.KafkaBroadcast;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KafkaBroadCastTests extends DistributionSyncStrategyTests {

	@Override
	protected AbstractDistributionSyncStrategy newInstance() {
//		不同字符串hashCode相同
//		System.out.println("ABCDEa123abc".hashCode());
//		System.out.println("ABCDFB123abc".hashCode());
		
		return new KafkaBroadcast("172.22.122.27:9092");
	}

}
