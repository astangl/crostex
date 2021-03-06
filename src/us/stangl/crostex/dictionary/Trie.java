/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.dictionary;

import java.util.ArrayList;
import java.util.List;

import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.ResettableIterator;

/**
 * Trie holding dictionary for crossword.
 * @author Alex Stangl
 */
public class Trie<E> implements Dictionary<char[], E> {
	/** head node of Trie */
	private TrieNode<E> head = new TrieNode<E>();

	/**
	 * get iterator to iterate over all words matching specified pattern
	 */
	public ResettableIterator<Pair<char[], E>> getIterator(char[] pattern) {
		return new TrieIterator<E>(head, pattern);
	}

	public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(200);
		for (ResettableIterator<Pair<char[], E>> it = getIterator(pattern); it.hasNext(); )
			retval.add(it.next());
		return retval;
	}

	/** Trie bulkInsert merely does a sequential insert */
	public void bulkInsert(List<Pair<char[], E>> entries) {
		for (Pair<char[], E> pair : entries)
			insert(pair.first, pair.second);
	}

	public void insert(char[] key, E word) {
		head.insert(key, word, 0);
	}

	public void remove(char[] key) {
		head.remove(key, 0);
	}

	public E lookup(char[] key) {
		return head.lookup(key, 0);
	}

	public boolean isPatternInDictionary(char[] key) {
		return head.isPatternInTrie(key, 0);
	}

    public void rebalance() {}
    
	private static class TrieNode<E> {
		/** reference to this node's attributes if it is a terminal node (i.e., ends a word) */
		private E word;

		/** references to other nodes for successor characters A .. Z */
		@SuppressWarnings("rawtypes")
		private TrieNode[] children = new TrieNode[26];

		public void insert(char[] key, E newWord, int keyIndex) {
			if (newWord == null)
				throw new IllegalArgumentException("Cannot insert null Word");
			if (keyIndex == key.length) {
				this.word = newWord;
			} else {
				char c = key[keyIndex];
				if (c < 'A' || c > 'Z')
					throw new IllegalArgumentException("Unrecognized character " + c + " at index " + keyIndex);
				int childIndex = c - 'A';
				@SuppressWarnings("unchecked")
				TrieNode<E> child = children[childIndex];
				if (child == null) {
					child = new TrieNode<E>();
					children[childIndex] = child;
				}
				child.insert(key, newWord, keyIndex + 1);
			}
		}

		public void remove(char[] key, int keyIndex) {
			// No pruning done since we only remove temporarily anyhow
			if (keyIndex == key.length) {
				word = null;
			} else {
				char c = key[keyIndex];
				if (c < 'A' || c > 'Z')
					throw new IllegalArgumentException("Unrecognized character " + c + " at index " + keyIndex);
				int childIndex = c - 'A';
				@SuppressWarnings("unchecked")
				TrieNode<E> child = children[childIndex];
				if (child != null)
					child.remove(key, keyIndex + 1);
			}
		}

		public E lookup(char[] key, int keyIndex) {
			if (keyIndex == key.length)
				return word;
			char c = key[keyIndex];
			if (c < 'A' || c > 'Z')
				throw new IllegalArgumentException("Unrecognized character " + c + " at index " + keyIndex);
			int childIndex = c - 'A';
			@SuppressWarnings("unchecked")
			TrieNode<E> child = children[childIndex];
			return child == null ? null : child.lookup(key, keyIndex + 1);
		}

		public boolean isPatternInTrie(char[] key, int keyIndex) {
			if (keyIndex == key.length)
				return word != null;
			char c = key[keyIndex];
			if (c == '_') {
				for (int i = 0; i < 26; ++i)
					if (children[i] != null && children[i].isPatternInTrie(key, keyIndex + 1))
						return true;
				return false;
			}

			if (c < 'A' || c > 'Z')
				throw new IllegalArgumentException("Unrecognized character " + c + " at index " + keyIndex);
			int childIndex = c - 'A';
			@SuppressWarnings("unchecked")
			TrieNode<E> child = children[childIndex];
			return child == null ? false : child.isPatternInTrie(key, keyIndex + 1);
		}

		public boolean isTerminal() {
			return word != null;
		}
	}

	private static class TrieIterator<E> implements ResettableIterator<Pair<char[], E>> {
		/** pattern */
		private final char[] pattern;

		/**
		 * index of child node for every node in current descent,
		 * starting with HEAD. Values point to NEXT element to return.
		 * If there is no next element, childIndexes[0] == -1
		 */
		private final int[] childIndexes;

		/**
		 * reference to TrieNode for each node in current descent,
		 * starting with HEAD. Points to NEXT element to return.
		 * If there is no next element, childIndexes[0] == -1
		 */
		private final TrieNode<E>[] descentGraph;

		@SuppressWarnings("unchecked")
		public TrieIterator(TrieNode<E> head, char[] pattern) {
			int length = pattern.length;
			this.pattern = pattern;

			childIndexes = new int[length];
			descentGraph = new TrieNode[length];
			descentGraph[0] = head;
			reset();
		}
		
		/** reset iterator to point to first element */
		public void reset() {
			// Now, drill down and find first word of length and leave iterator pointing to it
			// stick null at front of descentGraph if there are no elements
			if (! findFirstStartingAtDepth(0))
			   childIndexes[0] = -1;
		}
		
		public void avoidLetterAt(int index) {
			throw new UnsupportedOperationException("avoidLetterAt not implemented");
		}

		public void remove() {
			throw new UnsupportedOperationException("remove not implemented");
		}

		public boolean hasNext() {
			return childIndexes[0] != -1;
		}

		public Pair<char[], E> next() {
//			TrieTuple retval = new Tri
			char[] text = new char[childIndexes.length];
			for (int i = 0; i < childIndexes.length; ++i)
					text[i] = (char)(childIndexes[i] + 'A');

			Pair<char[], E> retval = new Pair<char[], E>(text, descentGraph[descentGraph.length - 1].word);
			// goto next element, if any
			if (! findNextStartingAtDepth(0))
				childIndexes[0] = -1;

			return retval;
		}
		/**
		 * Find first element, starting search at specified depth,
		 * where depth in 0 .. pattern_.length - 1
		 * Assumes descentGraph_[l] are set, where l in 0 .. depth
		 * Assumes childIndexes_[m] are set, where m in 0 .. depth - 1
		 * If found, return true and leave childIndexes and descentGraph_ pointing at it
		 */
		private boolean findFirstStartingAtDepth(int depth) {
			//TODO can optimize setting of childIndexes elements later
			char patChar = pattern[depth];
			if (depth == pattern.length - 1) {
				if (patChar != WILDCARD) {
					// If we are at bottom depth and we have no wildcard, then
					// we only have one possibility
					int index = patChar - 'A';
					@SuppressWarnings("unchecked")
					TrieNode<E> node = descentGraph[depth].children[index];
					if (node != null && node.isTerminal()) {
						childIndexes[depth] = index;
						return true;
					}
					return false;
				}
				// We are at bottom depth and have wildcard, need to iterate thru
				// possibilities
				for (int i = 0; i < 26; ++i) {
					@SuppressWarnings("unchecked")
					TrieNode<E> node = descentGraph[depth].children[i];
					if (node != null && node.isTerminal()) {
						childIndexes[depth] = i;
						return true;
					}
				}
				return false;                 // none found
			}
			// We are not at bottom depth
			if (patChar != WILDCARD) {
				// If we are not at bottom depth and we have no wildcard, then
				// we only have one possibility
				int index = patChar - 'A';
				@SuppressWarnings("unchecked")
				TrieNode<E> node = descentGraph[depth].children[index];
				if (node == null)
					return false;
				childIndexes[depth] = index;
				descentGraph[depth + 1] = node;
				return findFirstStartingAtDepth(depth + 1);
			}
			// We are not at bottom depth and have wildcard, need to iterate thru
			// possibilities
			for (int i = 0; i < 26; ++i) {
				@SuppressWarnings("unchecked")
				TrieNode<E> node = descentGraph[depth].children[i];
				if (node != null) {
					descentGraph[depth + 1] = node;
					childIndexes[depth] = i;
					if (findFirstStartingAtDepth(depth + 1))
						return true;
				}
			}
			return false;                    // none found
		}

		/**
		 * Find next element, starting search at specified depth,
		 * from 0 .. pattern_.length - 1
		 * Assumes all elements of descentGraph_ and childIndexes_ are set.
		 * If found, return true and leave childIndexes and descentGraph_ pointing at it
		 */
		private boolean findNextStartingAtDepth(int depth) {
			char patChar = pattern[depth];
			boolean atBottom = depth == pattern.length - 1;

			// First we try to do findNextStartingAtDepth all the way down to the
			// bottom. If bottom is a wildcard, we can try to iterate over them.
			// If nothing available at bottom, backup to last wildcard and try
			// findFirst on each until one returns true. If none return true,
			// return false, to try to backup to an earlier wildcard, etc.
			if (! atBottom && findNextStartingAtDepth(depth + 1))
				return true;

			// Either at bottom or we didn't find a next on our current branch below
			if (patChar != WILDCARD)
				return false;            // nothing good below us

			@SuppressWarnings("unchecked")
			TrieNode<E>[] children = descentGraph[depth].children;
			if (atBottom) {
				// We are at bottom depth and have wildcard, need to iterate thru possibilities
				while (++childIndexes[depth] < 26) {
					TrieNode<E> node = children[childIndexes[depth]];
					if (node != null && node.isTerminal())
						return true;
				}
				return false;                 // none found
			}

			// We are not at bottom depth and have wildcard, need to iterate thru possibilities
			while (++childIndexes[depth] < 26) {
				TrieNode<E> node = children[childIndexes[depth]];
				if (node != null) {
					descentGraph[depth + 1] = node;
					if (findFirstStartingAtDepth(depth + 1))
						return true;
				}
			}
			return false;                    // none found
		}

	}
}

