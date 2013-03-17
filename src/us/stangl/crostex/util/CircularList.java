/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.Collection;

/**
 * "List" that is circular in the sense that iterating over it using
 * getNext loops back from the end to the beginning, and deletePrev
 * loops back from the start to the end.
 * @author Alex Stangl
 */
public class CircularList<E> {
	/** number of elements in list */
	private int size;

	/** pointer to next node to return, node to add before, or null if list is empty */
	private Node<E> currNode;
	
	public int size() {
		return size;
	}
	
	public E getNext() {
		if (currNode == null)
			return null;
		E retval = currNode.entry;
		currNode = currNode.next;
		return retval;
	}

	public void add(E entry) {
		++size;
		Node<E> newNode = new Node<E>();
		newNode.entry = entry;
		if (currNode == null) {
			// list currently empty, make a 1 element list
			newNode.prev = newNode;
			newNode.next = newNode;
			currNode = newNode;
		} else {
			newNode.next = currNode;
			newNode.prev = currNode.prev;
			currNode.prev.next = newNode;
			currNode.prev = newNode;
		}
	}

	public void addAll(Collection<E> entries) {
		for (E entry : entries)
			add(entry);
	}

	public void deletePrev() {
		if (currNode == null)
			throw new IllegalStateException("Attempt to delete from empty circular list");
		if (--size == 0) {
			currNode = null;
		} else {
			Node<E> victim = currNode.prev;
			Node<E> nodeBeforeVictim = victim.prev;
			nodeBeforeVictim.next = currNode;
			currNode.prev = nodeBeforeVictim;
			victim.prev = null;
			victim.next = null;
		}
	}
	private static class Node<E> {
		private E entry;
		private Node<E> prev;
		private Node<E> next;
	}
}
