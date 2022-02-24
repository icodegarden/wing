package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.protect.KeyShardSynchronizedDoubleCheckProtector;

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
