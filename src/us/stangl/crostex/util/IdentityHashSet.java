/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of Set that uses object identity, rather than relying upon
 * .equals and .hashCode methods. 
 * Based upon IdentityHashMap -- IdentityHashSet is to IdentityHashMap as HashSet is to HashMap.
 * 
 * @author Alex Stangl
 */
public class IdentityHashSet<E> extends AbstractSet<E> implements Set<E> {

	private Map<E,Object> map = new IdentityHashMap<E,Object>();
	
	// dummy object value to associate with all map entries
	private static final Object DUMMY = new Object();
	

	/* (non-Javadoc)
	 * @see java.util.Set#size()
	 */
	@Override
	public int size() {
		return map.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		return map.put(e, DUMMY) == null;
	}

	/* (non-Javadoc)
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return map.remove(o) == DUMMY;
	}

	/* (non-Javadoc)
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear() {
		map.clear();
	}
}
