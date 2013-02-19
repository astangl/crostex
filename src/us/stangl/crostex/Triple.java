/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Generic triple for constructing simple 3-tuples.
 */
public class Triple<F, S, T> {
	public final F first;
	public final S second;
	public final T third;
	
	public Triple(F first, S second, T third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}
}
