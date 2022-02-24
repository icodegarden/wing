package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.protect.SynchronizedDoubleCheckProtector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class SynchronizedDoubleCheckProtectorTests extends ProtectorTests{
	
	@Override
	protected SynchronizedDoubleCheckProtector getProtector() {
		return new SynchronizedDoubleCheckProtector();
	}

	@Test
	public void testsynchronizer() throws Exception {
		SynchronizedDoubleCheckProtector protector = getProtector();
		
		String synchronizer = new String("synchronizer");
		
		assertEquals(protector, protector.synchronizer(synchronizer));
		
	}
}
