/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Tuple which contains contents of one element of a dictionary trie.
 */
public class TrieTuple {
	public final char[] text_;
	public final Word attributes_;
	
	public TrieTuple(char[] text, Word attributes) {
		text_ = text;
		attributes_ = attributes;
	}
}
