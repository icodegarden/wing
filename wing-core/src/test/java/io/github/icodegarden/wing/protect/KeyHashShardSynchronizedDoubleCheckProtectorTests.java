package io.github.icodegarden.wing.protect;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.protect.KeyHashShardSynchronizedDoubleCheckProtector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KeyHashShardSynchronizedDoubleCheckProtectorTests extends ProtectorTests{
	
	@Override
	protected KeyHashShardSynchronizedDoubleCheckProtector getProtector() {
		return new KeyHashShardSynchronizedDoubleCheckProtector(8);
	}

	@Test
	public void testsynchronizer() throws Exception {
		KeyHashShardSynchronizedDoubleCheckProtector protector = getProtector();
		for (int i = 0; i < 32267; i++) {
			Object node = protector.synchronizer(i + "");
			assertNotNull(node);
			node = protector.synchronizer(UUID.randomUUID().toString());
			assertNotNull(node);
		}
	}
}
