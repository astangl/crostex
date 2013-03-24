/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Exception thrown when error occurs during parsing of IPUZ data.
 * @author Alex Stangl
 */
public class IpuzParsingException extends Exception {

	private static final long serialVersionUID = 1L;

	public IpuzParsingException(String msg) {
		super(msg);
	}
	
	public IpuzParsingException(String msg, Throwable t) {
		super(msg, t);
	}
}
