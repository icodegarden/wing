package org.supermoto.yamaha.R1.io.cache.common;

/**
 * @author Fangfang.Xu
 */
public class EnvException extends CacheException {

	private static final long serialVersionUID = 1L;

	public EnvException(String message, Throwable cause) {
		super(message, cause);
	}

	public EnvException(String message) {
		super(message);
	}

	public EnvException(Throwable cause) {
		super(cause);
	}

}