package org.supermoto.yamaha.R1.io.cache.distribution.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.icodegarden.commons.lang.util.OSUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class NioBroadCastTests extends DistributionSyncStrategyTests {

	ArrayList<Discovery> discoverys = new ArrayList<Discovery>();
	Supplier<List<Discovery>> discoverysSupplier = () -> discoverys;

	int serverPort = 20000;

	@Override
	protected AbstractDistributionSyncStrategy newInstance() {
		final int port = serverPort++;

		Discovery discovery = new Discovery("applicationName", OSUtils.getIp()/* 不能用127.0.0.1 */, port);

		discoverys.add(discovery);

		return new NioBroadcast(port, discoverysSupplier);
	}

}
