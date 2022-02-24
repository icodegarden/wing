package org.supermoto.yamaha.R1.io.cache.distribution.sync;
/**
 * 
 * @author Fangfang.Xu
 *
 */
class OnSet extends DistributionSyncDTO {
	private Object v;
	private int expireSeconds;

	public Object getV() {
		return v;
	}

	public int getExpireSeconds() {
		return expireSeconds;
	}

	public void setV(Object v) {
		this.v = v;
	}

	public void setExpireSeconds(int expireSeconds) {
		this.expireSeconds = expireSeconds;
	}
}