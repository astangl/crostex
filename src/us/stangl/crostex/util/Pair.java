/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

/**
 * Generic pair for constructing simple tuples.
 * @author Alex Stangl
 */
public class Pair<F, S> {
	public final F first;
	public final S second;
	
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
}
