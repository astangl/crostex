/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Exception thrown when error occurs during serialization of IPUZ data.
 * @author Alex Stangl
 */
public class IpuzSerializationException extends Exception {

	private static final long serialVersionUID = 1L;

	public IpuzSerializationException(String msg) {
		super(msg);
	}
	
	public IpuzSerializationException(String msg, Throwable t) {
		super(msg, t);
	}
}
