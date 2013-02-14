/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.dictionary;

import java.util.List;

import us.stangl.crostex.Pair;
import us.stangl.crostex.util.ResettableIterator;

/**
 * Public interface for a dictionary class.
 */
public interface Dictionary<K, E> {
	/** wildcard character */
	static final char WILDCARD = '_';

	/**
	 * Return list of entries matching specified pattern.
	 * @param pattern
	 * @return list of entries matching specified pattern
	 */
	List<Pair<char[], E>> getPatternMatches(char[] pattern);

	/**
	 * Return resettable iterator over entries in the dictionary matching the specified pattern.
	 * @param pattern pattern to match
	 * @return resettable iterator over entries in the dictionary matching the specified pattern
	 */
	ResettableIterator<Pair<K, E>> getIterator(K pattern);

	/**
	 * Bulk insert sorted list of key/entries into dictionary.
	 * NOTE: This is the preferred approach to doing inserts, rather
	 * than individual inserts. This can result in a more balanced structure internally.
	 * NOTE: this method assumes the collection is sorted in ascending lexicographical order
	 */
	void bulkInsert(List<Pair<K, E>> entries);
	
	/**
	 * Insert one specified key into dictionary
	 * @param key key
	 * @param entry entry associated with key
	 */
	void insert(K key, E entry);

	/**
	 * Lookup key in dictionary, returning its associated entry, if found, else null.
	 * @param key key
	 * @return Entry associated with key if it is found, else null
	 */
	E lookup(K key);

	/**
	 * Return whether the specified pattern has any entries in this dictionary.
	 * @param pattern
	 * @return true if the pattern has at least 1 matching key in the dictionary
	 */
	boolean isPatternInDictionary(K pattern);
	
	/**
	 * Inform dictionary that all inserts have been completed, and to perform any desired optimizations,
	 * such as rebalancing.
	 */
	void rebalance();
}
