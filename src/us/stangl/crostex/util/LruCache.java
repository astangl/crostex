/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache. When cache reaches capacity, least-recently-used elements are discarded to make room for new elements.
 * @author Alex Stangl
 */
public class LruCache<K, V> {

	/** data holder */
	private final LinkedHashMap<K, V> cache;
	
	public LruCache(int capacity) {
		cache = new LruHashMap<K, V>(capacity);
	}
	
	public V get(K key) {
		return cache.get(key);
	}
	
	public void put(K key, V value) {
		cache.put(key, value);
	}
	
	private static class LruHashMap<K, V> extends LinkedHashMap<K, V> {
		private static final long serialVersionUID = 1L;

		/** saved capacity */
		private final int capacity;
		
		public LruHashMap(int capacity) {
			super(capacity, 0.75f, true);
			this.capacity = capacity;
		}
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	    	return size() > capacity;
	    }
	}
}
