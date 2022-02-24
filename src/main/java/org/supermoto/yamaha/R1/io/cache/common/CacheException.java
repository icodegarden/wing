package org.supermoto.yamaha.R1.io.cache.common;

/**
 * The base class of all other exceptions
 * 
 * @author Fangfang.Xu
 */
public abstract class CacheException extends RuntimeException {

	private final static long serialVersionUID = 1L;

	public CacheException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheException(String message) {
		super(message);
	}

	public CacheException(Throwable cause) {
		super(cause);
	}

	public CacheException() {
		super();
	}

}