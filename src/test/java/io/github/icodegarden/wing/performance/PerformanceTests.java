package io.github.icodegarden.wing.performance;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.Cacher;

public abstract class PerformanceTests {

	protected abstract Cacher newCacher();

	protected abstract String name();

	List<Integer> sizeOfKb = Arrays.asList(16, 64, 256);

	@BeforeEach
	void gc() {
		System.gc();
	}

	/**
	 * 单线程读
	 */
	@Test
	void singleThreadRead() throws Exception {
		Cacher cacher = newCacher();

		String key = "key";
		sizeOfKb.forEach(kb -> {
			gc();

			byte[] bs = new byte[kb * 1024];
			cacher.set(key, bs, 60);

			int loop = 10000;
			long start = System.currentTimeMillis();
			for (int i = 0; i < loop; i++) {
				cacher.get(key);
			}
			long end = System.currentTimeMillis();
			System.out.println("singleThreadRead " + name() + " loop:" + loop + " of size:" + kb + "KB cost millis:"
					+ (end - start));
		});
	}

	/**
	 * 单线程写
	 */
	@Test
	void singleThreadWrite() throws Exception {
		Cacher cacher = newCacher();

		int kb = 256;
		byte[] bs = new byte[kb * 1024];
		int loop = 10000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < loop; i++) {
			cacher.set(i % 16 + "", bs, 60);
		}
		long end = System.currentTimeMillis();
		System.out.println(
				"singleThreadWrite " + name() + " loop:" + loop + " of size:" + kb + "KB cost millis:" + (end - start));
	}

	/**
	 * 多线程读
	 */
	@Test
	void multiThreadRead() throws Exception {
		Cacher cacher = newCacher();

		String key = "key";
		sizeOfKb.forEach(kb -> {
			gc();

			byte[] bs = new byte[kb * 1024];
			cacher.set(key, bs, 60);
			int loop = 10000;
			int threads = 10;

			CountDownLatch countDownLatch = new CountDownLatch(threads);
			long start = System.currentTimeMillis();
			for (int t = 0; t < threads; t++) {
				new Thread() {
					public void run() {
						for (int i = 0; i < loop / threads; i++) {
							cacher.get(key);
						}
						countDownLatch.countDown();
					}
				}.start();
			}
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}
			long end = System.currentTimeMillis();
			System.out.println("multiThreadRead " + name() + " threads:" + threads + " loop:" + loop + " of size:" + kb
					+ "KB cost millis:" + (end - start));
		});
	}

	/**
	 * 多线程写
	 */
	@Test
	void multiThreadWrite() throws Exception {
		Cacher cacher = newCacher();

		int kb = 256;
		byte[] bs = new byte[kb * 1024];
		int loop = 10000;
		int threads = 10;

		CountDownLatch countDownLatch = new CountDownLatch(threads);
		long start = System.currentTimeMillis();
		for (int t = 0; t < threads; t++) {
			new Thread() {
				public void run() {
					for (int i = 0; i < loop / threads; i++) {
						cacher.set(i % 16 + "", bs, 60);
					}
					countDownLatch.countDown();
				}
			}.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
		}
		long end = System.currentTimeMillis();
		System.out.println("multiThreadWrite " + name() + " threads:" + threads + " loop:" + loop + " of size:" + kb
				+ "KB cost millis:" + (end - start));
	}
}
