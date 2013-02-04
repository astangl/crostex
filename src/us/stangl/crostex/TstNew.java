/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import us.stangl.crostex.util.ResettableIterator;

/**
 * Ternary search tree implementation of Dictionary.
 */
public class TstNew<E> implements Dictionary<char[], E> {
	/** wildcard character */
	public static final char WILDCARD = '_';

	/** each element at index N in forest_ contains an array of TST heads for words of size N + 3 */
	private List<TstNode<E>[]> forest_ = new ArrayList<TstNode<E>[]>();

	private void growForestToSize(int size) {
		while (forest_.size() < size)
			forest_.add(new TstNode[26]);
	}

	/**
	 * Return resettable iterator over entries in the dictionary matching the specified pattern.
	 * @param pattern pattern to match
	 * @return resettable iterator over entries in the dictionary matching the specified pattern
	 */
	public ResettableIterator<Pair<char[], E>> getIterator(char[] pattern) {
		return new TstIterator<E>(getPatternMatches(pattern));
	}

	/**
	 * Bulk insert sorted list of key/entries into dictionary.
	 * NOTE: This is the preferred approach to doing inserts, rather
	 * than individual inserts. This can result in a more balanced structure internally.
	 * NOTE: this method assumes the collection is sorted in ascending lexicographical order
	 */
	public void bulkInsert(List<Pair<char[], E>> entries) {
		bulkInsert(entries, 0, entries.size() - 1);
	}

	/**
	 * Insert one specified key into dictionary
	 * @param key key
	 * @param entry entry associated with key
	 */
	public void insert(char[] key, E entry) {
		int headIndex = key[0] - 'A';
		int forestIndex = key.length - 3;
		growForestToSize(forestIndex + 1);
		TstNode<E>[] heads = forest_.get(forestIndex);
		heads[headIndex] = add(heads[headIndex], key, 1, entry);
	}

	/**
	 * Lookup key in dictionary, returning its associated entry, if found, else null.
	 * Assumes length is at least 1.
	 * @param key key
	 * @return Entry associated with key if it is found, else null
	 */
	public E lookup(char[] key) {
		int forestIndex = key.length - 3;
		if (forest_.size() <= forestIndex)
			return null;
		//TstNode<E>[] heads = forest_.get(forestIndex);
		//return lookup(heads[key[0] - 'A'], key, 1);
		TstNode<E> node = forest_.get(forestIndex)[key[0] - 'A'];
		int keyIndex = 1;
		char c = key[keyIndex];
		while (true) {
			if (node == null)
				return null;
			if (c < node.splitChar_)
				node = node.leftChild_;
			else if (c > node.splitChar_)
				node = node.rightChild_;
			else if (keyIndex < key.length - 1) {
				node = (TstNode<E>)node.middleChild_;
				c = key[++keyIndex];
			} else {
				return (E)node.middleChild_;
			}
		}
	}

	public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		int forestIndex = pattern.length - 3;
		if (forest_.size() <= forestIndex)
			return Collections.EMPTY_LIST;
		TstNode<E>[] heads = forest_.get(forestIndex);
		// start with 200 capacity: 760 extra bytes to save up to 7 realloc/copies
		List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(200);
		char[] keybuff = new char[pattern.length];
		char firstChar = pattern[0];

		if (firstChar != WILDCARD) {
			keybuff[0] = firstChar;
			TstNode<E> head = heads[firstChar - 'A'];
			if (head != null)
				getPatternMatches(head, pattern, 1, retval, keybuff);
		} else {
			keybuff[0] = 'A';
			for (TstNode<E> head : heads) {
				if (head != null)
					getPatternMatches(head, pattern, 1, retval, keybuff);
				++keybuff[0];
			}
		}
		
		// If empty, return empty list, so we don't waste memory if it gets cached
		return retval.size() > 0 ? retval : Collections.EMPTY_LIST;
	}

	private void getPatternMatches(TstNode<E> node, char[] pattern, int keyIndex, List<Pair<char[], E>> accumulator, char[] keybuff) {
		char c = pattern[keyIndex];
		//TODO consider splitting out wildcard path, but clutters code more, potentially kills L1 cache
		if ((c == WILDCARD || c < node.splitChar_) && node.leftChild_ != null)
			getPatternMatches(node.leftChild_, pattern, keyIndex, accumulator, keybuff);
		if ((c == WILDCARD || c == node.splitChar_) && node.middleChild_ != null) {
			keybuff[keyIndex] = node.splitChar_;
			if (keyIndex < pattern.length - 1) {
				getPatternMatches((TstNode<E>)node.middleChild_, pattern, keyIndex + 1, accumulator, keybuff);
			} else {
				// have to copy keybuff since it's mutable
				char[] key = new char[pattern.length];
				for (int i = 0; i < key.length; ++i)
					key[i] = keybuff[i];
//System.out.println("Adding word " + new String(key) + " for pattern " + new String(pattern));				
				accumulator.add(new Pair<char[], E>(key, (E)node.middleChild_));
			}
		}
		if ((c == WILDCARD || c > node.splitChar_) && node.rightChild_ != null)
			getPatternMatches(node.rightChild_, pattern, keyIndex, accumulator, keybuff);
	}

	/**
	 * Return whether the specified pattern has any entries in this dictionary.
	 * @param pattern
	 * @return true if the pattern has at least 1 matching key in the dictionary
	 */
	public boolean isPatternInDictionary(char[] pattern) {
		int forestIndex = pattern.length - 3;
		if (forest_.size() <= forestIndex)
			return false;
		TstNode<E>[] heads = forest_.get(forestIndex);
		if (pattern[0] != WILDCARD)
			return isPatternFound(heads[pattern[0] - 'A'], pattern, 1);

		for (TstNode<E> head : heads)
			if (isPatternFound(head, pattern, 1))
				return true;
		return false;
	}

	private boolean isPatternFound(TstNode<E> node, char[] key, int keyIndex) {
		if (node == null)
			return false;
		char c = key[keyIndex];
		if ((c == WILDCARD || c < node.splitChar_) && isPatternFound(node.leftChild_, key, keyIndex))
			return true;
		if (c == WILDCARD || c == node.splitChar_) {
			if (keyIndex < key.length - 1) {
				if (isPatternFound((TstNode<E>)node.middleChild_, key, keyIndex + 1))
					return true;
			} else if (node.middleChild_ != null) {
				return true;
			}
		}
		return ((c == WILDCARD || c > node.splitChar_) && isPatternFound(node.rightChild_, key, keyIndex));
	}

	/*
	private E lookup(TstNode<E> node, char[] key, int keyIndex) {
		char c = key[keyIndex];
		while (true) {
			if (node == null)
				return null;
			if (c < node.splitChar_)
				node = node.leftChild_;
			else if (c > node.splitChar_)
				node = node.rightChild_;
			else if (keyIndex < key.length - 1) {
				node = (TstNode<E>)node.middleChild_;
				c = key[++keyIndex];
			} else {
				return (E)node.middleChild_;
			}
		}
		
		/** recursive version below
		if (node == null)
			return null;
		char c = key[keyIndex];
		if (c < node.splitChar_)
			return lookup(node.leftChild_, key, keyIndex);
		if (c > node.splitChar_)
			return lookup(node.rightChild_, key, keyIndex);
		if (keyIndex < key.length - 1)
			return lookup((TstNode<E>)node.middleChild_, key, keyIndex + 1);
		return (E)node.middleChild_;
	}
		*/

	/** recursively insert middle entry from subarray, then left and right subarrays to get balanced TST */
	private void bulkInsert(List<Pair<char[], E>> entries, int lowIndex, int highIndex) {
		if (lowIndex > highIndex)
			return;						// we're done with this branch

		// First insert middle element from our subarray...
		int middleIndex = (highIndex + lowIndex) / 2;
		Pair<char[], E> middleEntry = entries.get(middleIndex);
		insert(middleEntry.first_, middleEntry.second_);

		// ...now recursively do left and right subarrays
		bulkInsert(entries, lowIndex, middleIndex - 1);
		bulkInsert(entries, middleIndex + 1, highIndex);
	}

	private TstNode<E> add(TstNode<E> node, char[] key, int keyIndex, E entry) {
		char c = key[keyIndex];
		if (node == null)
			node = new TstNode<E>(c);
		if (c < node.splitChar_)
			node.leftChild_ = add(node.leftChild_, key, keyIndex, entry);
		else if (c > node.splitChar_)
			node.rightChild_ = add(node.rightChild_, key, keyIndex, entry);
		else if (keyIndex < key.length - 1)
			node.middleChild_ = add((TstNode<E>)node.middleChild_, key, keyIndex + 1, entry);
		else
			node.middleChild_ = entry;
		return node;
	}

	public String toString() {
		StringBuilder retval = new StringBuilder();
		retval.append("{");
		for (int j = 0; j < forest_.size(); ++j) {
			TstNode<E>[] heads = forest_.get(j);
			if (j > 0)
				retval.append(", ");
			retval.append("forest[").append(j).append("] = ");
			for (int i = 0; i < heads.length; ++i) {
				TstNode<E> head = heads[i];
				if (i > 0)
					retval.append(", ");
				String headString = head == null ? "null" : head.toString();
				retval.append("head_[").append(i).append("] = ").append(headString);
			}
		}
		retval.append("}");
		return retval.toString();
	}

	// DSW tree compression, vine -> tree, do count # of left rotations starting at root
	private void compression(TstNode<E> root, int count) {
		TstNode<E> scanner = root;
		for (int j = 0; j < count; ++j) {
			// Leftward rotation
			TstNode<E> child = scanner.rightChild_;
			scanner.rightChild_ = child.rightChild_;
			scanner = scanner.rightChild_;
			child.rightChild_ = scanner.leftChild_;
			scanner.leftChild_ = child;
		}
	}
	
	/**
	 * turn tree into a "vine", collapsed down to being all linked along right links
	 * @return size, not including pseudo-root
	 */
	private int treeToVine(TstNode<E> pseudoRoot) {
		int size = 0;
		TstNode<E> vineTail, remainder;
		vineTail = pseudoRoot;					// vineTail always points to end of vine linked-list
		remainder = vineTail.rightChild_;		// remainder points to head of part that still needs work
		while (remainder != null) {
			if (remainder.leftChild_ == null) {
				// Recursively rebalance middle tree, if there is one
				if (remainder.middleChild_ instanceof TstNode)
					remainder.middleChild_ = rebalanceSubtree((TstNode<E>)remainder.middleChild_);

				// No leftward subtree, move rightward
				vineTail = remainder;
				remainder = remainder.rightChild_;
				++size;
			} else {
				// Eliminate leftward subtree by rightward rotation
				TstNode<E> tempPtr = remainder.leftChild_;
				remainder.leftChild_ = tempPtr.rightChild_;
				tempPtr.rightChild_ = remainder;
				remainder = tempPtr;
				vineTail.rightChild_ = tempPtr;
			}
		}
		return size;
	}

	private int fullSize(int size) {
		int retval = 1;
		while (retval <= size)		// drive one step PAST FULL
			retval += retval + 1;	// next pow(2,k) - 1
		return retval / 2;
	}

	private void vineToTree(TstNode<E> root, int size) {
		int fullCount = fullSize(size);
		compression(root, size - fullCount);
		size = fullCount;
		while (size > 1) {
			size /= 2;
			compression(root, size);
		}
	}
	
	/** rebalance subtree, returning new root */
	private TstNode<E> rebalanceSubtree(TstNode<E> root) {
		TstNode<E> pseudoRoot = new TstNode<E>(' ');
		pseudoRoot.rightChild_ = root;
		vineToTree(pseudoRoot, treeToVine(pseudoRoot));
		return pseudoRoot.rightChild_;
	}
	
	public void rebalance() {
		for (TstNode<E>[] rootsArray : forest_)
			for (int i = 0; i < rootsArray.length; ++i)
				rootsArray[i] = rebalanceSubtree(rootsArray[i]);
	}
    

	private static class TstNode<E> {
		/** split character */
		private final char splitChar_;

		/** left child contains all children whose curr. char is less than our split char */
		private TstNode<E> leftChild_;

		/**
		 * in interior node, middle child contains all children whose curr. char is our split char
		 * in leaf node, middle child points to entry
		 */
		private Object middleChild_;

		/** right child contains all children whose curr. char is greater than our split char */
		private TstNode<E> rightChild_;

		public TstNode(char splitChar) {
			splitChar_ = splitChar;
		}

		public String toString() {
			StringBuilder retval = new StringBuilder();
			String leftChildString = leftChild_ == null ? "null" : leftChild_.toString();
			String middleChildString = middleChild_ == null ? "null" : middleChild_.toString();
			String rightChildString = rightChild_ == null ? "null" : rightChild_.toString();
			retval.append("{").append("splitChar_ = ").append(splitChar_)
				.append(", leftChild_ = ").append(leftChildString)
				.append(", middleChild_ = ").append(middleChildString)
				.append(", rightChild_ = ").append(rightChildString)
				.append("}");
			return retval.toString();
		}
	}


	private static class TstIterator<E> implements ResettableIterator<Pair<char[], E>> {
		/** list of entries to iterate over */
		private final List<Pair<char[], E>> entries_;

		/** pointer to next entry to return */
		private int nextPointer_;

		public TstIterator(List<Pair<char[], E>> entries) {
			entries_ = entries;
			reset();
		}

		/**
		 * Reset iterator back to its initial creation state.
		 */
		public void reset() {
			nextPointer_ = 0;
		}

		/**
		 * @see us.stangl.crostex.util.ResettableIterator#avoidLetterAt(int)
		 */
		public void avoidLetterAt(int index) {
		}

		public void remove() {
			throw new UnsupportedOperationException("remove not implemented");
		}

		/**
		 * @return whether any elements are remaining (i.e, safe to call next).
		 */
		public boolean hasNext() {
			return nextPointer_ < entries_.size();
		}

		public Pair<char[], E> next() {
			Pair<char[], E> retval = peekNext();
			++nextPointer_;
			return retval;
		}

		/** just like next but without advancing iterator */
		public Pair<char[], E> peekNext() {
			return entries_.get(nextPointer_);
		}
	}
}
