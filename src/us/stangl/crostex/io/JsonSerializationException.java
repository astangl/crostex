/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Exception thrown if error occurs during JSON serialization.
 * @author Alex Stangl
 */
public class JsonSerializationException extends Exception {

	private static final long serialVersionUID = 1L;

	public JsonSerializationException(String msg) {
		super(msg);
	}
	
	public JsonSerializationException(String msg, Throwable t) {
		super(msg, t);
	}
}
