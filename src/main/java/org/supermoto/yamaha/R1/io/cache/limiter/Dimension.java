package org.supermoto.yamaha.R1.io.cache.limiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流维度
 * 
 * @author Fangfang.Xu
 *
 */
public class Dimension {

	private String name;

	private long lastResetTime;

	private long interval;

	private AtomicInteger token;

	private int rate;

	/**
	 * 
	 * @param name
	 * @param rate     在给定的interval中允许的次数
	 * @param interval 刷新间隔millis
	 */
	public Dimension(String name, int rate, long interval) {
		if (name == null) {
			throw new IllegalArgumentException("name must not null");
		}
		if (rate <= 0) {
			throw new IllegalArgumentException("rate must gt 0");
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must gt 0");
		}
		this.name = name;
		this.rate = rate;
		this.interval = interval;
	}

	Dimension start() {
		this.lastResetTime = System.currentTimeMillis();
		this.token = new AtomicInteger(rate);
		return this;
	}

	public String getName() {
		return name;
	}

	public long getLastResetTime() {
		return lastResetTime;
	}

	public long getInterval() {
		return interval;
	}

	public int getRate() {
		return rate;
	}

	boolean isAllowable() {
		long now = System.currentTimeMillis();
		if (now > lastResetTime + interval) {
			token = new AtomicInteger(rate);
			lastResetTime = now;
		}
		if (token.intValue() < 1) {
			return false;
		}
		token.decrementAndGet();
		return true;
	}
}