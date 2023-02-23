package io.github.icodegarden.wing.common;

import io.github.icodegarden.commons.lang.spec.response.ClientParameterInvalidErrorCodeException;

/**
 * The base class of all other exceptions
 * 
 * @author Fangfang.Xu
 */
public abstract class ClientCacheException extends ClientParameterInvalidErrorCodeException {

	private final static long serialVersionUID = 1L;

	public ClientCacheException(String message) {
		super(ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), message);
	}

	public ClientCacheException(String message, Throwable cause) {
		super(ClientParameterInvalidErrorCodeException.SubPair.INVALID_PARAMETER.getSub_code(), message);

		initCause(cause);
	}

}