package org.supermoto.yamaha.R1.io.cache.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.supermoto.yamaha.R1.io.cache.protect.KeyShardSynchronizedDoubleCheckProtector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KeyShardSynchronizedDoubleCheckProtectorTests extends ProtectorTests{
	
	@Override
	protected KeyShardSynchronizedDoubleCheckProtector getProtector() {
		return new KeyShardSynchronizedDoubleCheckProtector();
	}

	@Test
	public void testsynchronizer() throws Exception {
		KeyShardSynchronizedDoubleCheckProtector protector = getProtector();
		
		String synchronizer = new String("synchronizer");
		
		assertEquals(synchronizer, protector.synchronizer(synchronizer));
		
	}
}
