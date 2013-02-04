/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Generic pair for constructing simple tuples.
 */
public class Pair<F, S> {
	public final F first_;
	public final S second_;
	
	public Pair(F first, S second) {
		first_ = first;
		second_ = second;
	}
}
