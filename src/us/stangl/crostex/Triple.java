/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Generic triple for constructing simple 3-tuples.
 */
public class Triple<F, S, T> {
	public final F first_;
	public final S second_;
	public final T third_;
	
	public Triple(F first, S second, T third) {
		first_ = first;
		second_ = second;
		third_ = third;
	}
}
