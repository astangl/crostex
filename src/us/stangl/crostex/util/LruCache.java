/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU cache. When cache reaches capacity, least-recently-used elements are discarded to make room for new elements.
 */
public class LruCache<K, V> {

	/** data holder */
	private final LinkedHashMap<K, V> cache_;
	
	public LruCache(int capacity) {
		cache_ = new LruHashMap<K, V>(capacity);
	}
	
	public V get(K key) {
		return cache_.get(key);
	}
	
	public void put(K key, V value) {
		cache_.put(key, value);
	}
	
	private static class LruHashMap<K, V> extends LinkedHashMap<K, V> {
		/** saved capacity */
		private final int capacity_;
		
		public LruHashMap(int capacity) {
			super(capacity, 0.75f, true);
			capacity_ = capacity;
		}
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	    	return size() > capacity_;
	    }
	}
}
