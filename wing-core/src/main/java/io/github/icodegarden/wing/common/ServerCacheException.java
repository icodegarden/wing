package io.github.icodegarden.wing.common;

import io.github.icodegarden.commons.lang.spec.response.ServerErrorCodeException;

/**
 * The base class of all other exceptions
 * 
 * @author Fangfang.Xu
 */
public abstract class ServerCacheException extends ServerErrorCodeException {

	private final static long serialVersionUID = 1L;

	public ServerCacheException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerCacheException(Throwable cause) {
		super(cause);
	}

}