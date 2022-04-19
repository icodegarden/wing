package io.github.icodegarden.wing.common;

/**
 * @author Fangfang.Xu
 */
public class RejectedRequestException extends CacheException {

	private static final long serialVersionUID = 1L;

	private final Object rejector;
	private final String reason;

	public RejectedRequestException(Object rejector, String reason) {
		super(reason);
		this.rejector = rejector;
		this.reason = reason;
	}
	
	public RejectedRequestException(Object rejector, String reason, Throwable cause) {
		super(reason, cause);
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