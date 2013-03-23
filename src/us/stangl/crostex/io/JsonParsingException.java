/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Exception thrown if error occurs during JSON parsing.
 * @author Alex Stangl
 */
public class JsonParsingException extends Exception {

	public JsonParsingException(String msg) {
		super(msg);
	}
	
	public JsonParsingException(String msg, Throwable t) {
		super(msg, t);
	}
}
