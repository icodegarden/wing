package org.supermoto.yamaha.R1.io.cache.distribution.sync;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Discovery {
	private final String applicationName;
	private final String address;
	private final int port;

	public Discovery(String applicationName, String address, int port) {
		this.applicationName = applicationName;
		this.address = address;
		this.port = port;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((applicationName == null) ? 0 : applicationName.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public String toString() {
		return "Discovery [applicationName=" + applicationName + ", address=" + address + ", port=" + port + "]";
	}
}