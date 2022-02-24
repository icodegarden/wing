package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class SyncFailedException extends CacheException {

	private static final long serialVersionUID = 1L;

	public SyncFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SyncFailedException(String message) {
		super(message);
	}

	public SyncFailedException(Throwable cause) {
		super(cause);
	}

}