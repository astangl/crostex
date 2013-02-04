/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.Collection;

/**
 * 
 */
public class CircularList<E> {
	/** number of elements in list */
	private int size_;

	/** pointer to next node to return, node to add before, or null if list is empty */
	private Node<E> currNode_;
	
	public int size() {
		return size_;
	}
	
	public E getNext() {
		if (currNode_ == null)
			return null;
		E retval = currNode_.entry_;
		currNode_ = currNode_.next_;
		return retval;
	}

	public void add(E entry) {
		++size_;
		Node<E> newNode = new Node<E>();
		newNode.entry_ = entry;
		if (currNode_ == null) {
			// list currently empty, make a 1 element list
			newNode.prev_ = newNode;
			newNode.next_ = newNode;
			currNode_ = newNode;
		} else {
			newNode.next_ = currNode_;
			newNode.prev_ = currNode_.prev_;
			currNode_.prev_.next_ = newNode;
			currNode_.prev_ = newNode;
		}
	}

	public void addAll(Collection<E> entries) {
		for (E entry : entries)
			add(entry);
	}

	public void deletePrev() {
		if (currNode_ == null)
			throw new IllegalStateException("Attempt to delete from empty circular list");
		if (--size_ == 0) {
			currNode_ = null;
		} else {
			Node<E> victim = currNode_.prev_;
			Node<E> nodeBeforeVictim = victim.prev_;
			nodeBeforeVictim.next_ = currNode_;
			currNode_.prev_ = nodeBeforeVictim;
			victim.prev_ = null;
			victim.next_ = null;
		}
	}
	private static class Node<E> {
		private E entry_;
		private Node prev_;
		private Node next_;
	}
}
