/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Exception thrown whenever a service cannot be completed successfully.
 */
public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor taking just a message. 
	 * @param message descriptive message
	 */
	public ServiceException(String message) {
		super(message);
	}

	/**
	 * Constructor taking a message and an underlying cause. 
	 * @param message descriptive message
	 * @param cause underlying cause
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
