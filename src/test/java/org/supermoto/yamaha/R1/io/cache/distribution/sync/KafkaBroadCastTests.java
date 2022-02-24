package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import org.supermoto.yamaha.R1.io.cache.distribution.sync.AbstractDistributionSyncStrategy;
import org.supermoto.yamaha.R1.io.cache.distribution.sync.KafkaBroadcast;

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
