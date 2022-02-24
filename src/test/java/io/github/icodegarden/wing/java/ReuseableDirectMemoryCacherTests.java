package io.github.icodegarden.wing.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.java.ReuseableDirectMemoryCacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class ReuseableDirectMemoryCacherTests extends DefaultDirectMemoryCacherTests {

	ReuseableDirectMemoryCacher cacher;

	@Override
	protected Cacher getCacher() {
		if(cacher != null) {
			return cacher;
		}
		cacher = new ReuseableDirectMemoryCacher();
		return cacher;
	}
	@Test
	public void testClearIdles() throws Exception {
		int sizeFactor = 1;//需要精确的，不然重用时这里不好判断
		//测试idle时间超过的清理
		cacher.setSizeFactor(sizeFactor);
		cacher.setMaxIdleSeconds(1);
		
		cacher.set(key, v, expireSeconds);
		cacher.set(key2, v2, expireSeconds);
		assertEquals(0, cacher.idleSize());
		
		cacher.remove(key);
		cacher.remove(key2);
		assertEquals(2, cacher.idleSize());
		
		Thread.sleep(3000);
		cacher.clearIdles();
		assertEquals(0, cacher.idleSize());
		
		//测试idle数量超过的清理
		cacher.setMaxIdles(2);//最多2个
		cacher.setMaxIdleSeconds(60);
		
		cacher.set(key, v, expireSeconds);
		cacher.set(key2, v2, expireSeconds);
//		Thread.sleep(1000);//3和4比1、2晚创建
		cacher.set(key3, v3, expireSeconds);
		cacher.set(key4, v4, expireSeconds);
		assertEquals(0, cacher.idleSize());
		
		cacher.remove(Arrays.asList(key,key2,key3,key4));
		assertEquals(4, cacher.idleSize());
		
		cacher.set(key2, v2, expireSeconds);//2、4进行重用
		cacher.set(key4, v4, expireSeconds);
		cacher.remove(Arrays.asList(key,key2,key3,key4));
		assertEquals(4, cacher.idleSize());
		
		Thread.sleep(1000);//需要停顿一下，否则时间指数都是0
		cacher.clearIdles();//1、3将被回收
		assertEquals(2, cacher.idleSize());
		//剩余的2和4
		assertEquals(
				sizeFactor * cacher.spaceSizeCalc(v2) + sizeFactor * cacher.spaceSizeCalc(v4),
				cacher.idleCapacity());
		
		
	}
}
