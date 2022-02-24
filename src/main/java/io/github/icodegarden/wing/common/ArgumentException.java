package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class ArgumentException extends CacheException {

	private static final long serialVersionUID = 1L;

	public ArgumentException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentException(String message) {
		super(message);
	}

	public ArgumentException(Throwable cause) {
		super(cause);
	}

}