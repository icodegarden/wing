package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.protect.SynchronizedDoubleCheckProtector;

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
