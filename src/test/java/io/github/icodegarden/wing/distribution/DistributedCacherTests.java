package io.github.icodegarden.wing.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.distribution.DistributedCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DistributedCacherTests {

	@Test
	public void shouldWriteOpThread() throws Exception {
		DistributedCacher distributedCacher = spy(DistributedCacher.class);
		new Thread() {
			@Override
			public void run() {
				try {
					assertEquals(true, distributedCacher.shouldWriteOpThread());
					
					DistributedCacher.shouldWriteOpThread(true);
					assertEquals(true, distributedCacher.shouldWriteOpThread());
					
					DistributedCacher.shouldWriteOpThread(false);
					assertEquals(false, distributedCacher.shouldWriteOpThread());
				} catch (Throwable e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				try {
					assertEquals(true, distributedCacher.shouldWriteOpThread());
					
					DistributedCacher.shouldWriteOpThread(true);
					assertEquals(true, distributedCacher.shouldWriteOpThread());
					
					DistributedCacher.shouldWriteOpThread(false);
					assertEquals(false, distributedCacher.shouldWriteOpThread());
				} catch (Throwable e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}.start();
		
		Thread.sleep(1000);
	}

}
