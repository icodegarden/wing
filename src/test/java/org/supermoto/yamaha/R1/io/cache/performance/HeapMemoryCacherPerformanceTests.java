package org.supermoto.yamaha.R1.io.cache.performance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import org.supermoto.yamaha.R1.io.cache.Cacher;
import org.supermoto.yamaha.R1.io.cache.java.HeapMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HeapMemoryCacherPerformanceTests extends PerformanceTests {

	HeapMemoryCacher heapMemoryCacher;

	@Override
	protected Cacher newCacher() {
		return new HeapMemoryCacher();
	}

	@Override
	protected String name() {
		return "HeapMemoryCacher";
	}
//	private volatile SortedMap<Integer, String> idles = new TreeMap<>();
	private volatile ConcurrentSkipListMap<Integer, String> idles = new ConcurrentSkipListMap<>();
//	private volatile SortedMap<Integer, String> idles = Collections.synchronizedSortedMap(new TreeMap<>());
	@org.junit.jupiter.api.Test
	void testName() throws Exception {
		new Thread() {
			@Override
			public void run() {
				for(int i=0;i<100000;i++) {
					idles.put(i, i+"");
				}
			}
		}.start();
		
//		Thread.sleep(1000);
//		
//		System.out.println(idles.size());
		
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for(int i=0;i<100000;i++) {
					idles.entrySet().removeIf(ii->true);
				}
			}
		}.start();
		Thread.sleep(1000);
		
		
		System.out.println(idles.size());
		
//		new Thread() {
//			@Override
//			public void run() {
//				for(;;) {
//					idles.entrySet().stream().filter(entry -> {
//						String memory = entry.getValue();
//						return memory != null;
//					})/*需要进行collect，否则是并发异常*/.collect(Collectors.toList()).forEach(entry -> {
//						idles.remove(entry.getKey());
//					});
//				}
//			}
//		}.start();
		
//		new Thread() {
//			@Override
//			public void run() {
//				for(;;) {
//					List<Entry<Integer, String>> collect = idles.entrySet().stream().filter(entry -> {
//						String memory = entry.getValue();
//						return memory != null;
//					}).collect(Collectors.toList());
//				}
//			}
//		}.start();
		
	}
}
