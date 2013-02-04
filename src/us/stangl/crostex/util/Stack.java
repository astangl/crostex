/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

/**
 * Simple, efficient, non-synchronized LIFO stack.
 * Originally threw EmptyStackException, but really what's the point? Pay extra
 * cost for every pop/peek, just to have a "more descriptive" exception,
 * EmptyStackException instead of ArrayIndexOutOfBoundsException? No thanks. 
 */
public class Stack<E> {
	/** slots for holding elements */
	private E[] slots_;
	
	/** fill pointer, index of next member of slots_ to write into. If equal 0, stack is empty */
	private int fillPointer_;
	
	/**
	 * Constructor specifying initial capacity
	 * @param initialCapacity
	 */
	public Stack(int initialCapacity) {
		slots_ = (E[])new Object[initialCapacity];
	}
	
	/**
	 * No-arg constructor, for default initial capacity.
	 */
	public Stack() {
		this(200);
	}

	public void push(E entry) {
		if (fillPointer_ >= slots_.length)
			growSlots();
		slots_[fillPointer_++] = entry;
	}
	
	public boolean empty() {
		return fillPointer_ == 0;
	}

	/**
	 * return last pushed element without removing it from the stack
	 * @throws ArrayIndexOutOfBoundsException if stack empty
	 */
	public E peek() {
		return slots_[fillPointer_ - 1];
	}
	
	/**
	 * return last pushed element, popping it off the stack
	 * @throws ArrayIndexOutOfBoundsException if stack empty
	 */
	public E pop() {
		return slots_[--fillPointer_];
	}
	
	public boolean contains(E obj) {
		for (int i = 0; i < fillPointer_; ++i)
			if (slots_[i] == obj)
				return true;
		return false;
	}
	
	private void growSlots() {
		E[] newSlots = (E[])new Object[(slots_.length * 3) / 2 + 1];
		for (int i = 0; i < slots_.length; ++i)
			newSlots[i] = slots_[i];
		slots_ = newSlots;
	}
}
