package org.supermoto.yamaha.R1.io.cache.distribution.sync;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class DistributionSyncDTO {
	private String applicationName;
	private String applicationInstanceId;
	private String key;

	public String getApplicationName() {
		return applicationName;
	}

	public String getApplicationInstanceId() {
		return applicationInstanceId;
	}

	public String getKey() {
		return key;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setApplicationInstanceId(String applicationInstanceId) {
		this.applicationInstanceId = applicationInstanceId;
	}

	public void setKey(String key) {
		this.key = key;
	}
}