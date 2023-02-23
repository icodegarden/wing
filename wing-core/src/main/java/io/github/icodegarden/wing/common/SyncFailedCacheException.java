package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class SyncFailedCacheException extends ServerCacheException {

	private static final long serialVersionUID = 1L;

	public SyncFailedCacheException(String message, Throwable cause) {
		super(message, cause);
	}

}