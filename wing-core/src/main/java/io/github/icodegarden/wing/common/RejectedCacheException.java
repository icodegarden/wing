package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class RejectedCacheException extends ClientCacheException {

	private static final long serialVersionUID = 1L;

	private final Object rejector;
	private final String reason;

	public RejectedCacheException(Object rejector, String reason) {
		super(reason);
		this.rejector = rejector;
		this.reason = reason;
	}
	
	public Object getRejector() {
		return rejector;
	}
	
	public String getReason() {
		return reason;
	}
}