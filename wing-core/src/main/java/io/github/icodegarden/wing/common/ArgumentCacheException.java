package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class ArgumentCacheException extends ClientCacheException {

	private static final long serialVersionUID = 1L;

	public ArgumentCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentCacheException(String message) {
		super(message);
	}

}