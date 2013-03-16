/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.ResettableIterator;


/**
 * Implementation of dictionary optimized for crossword pattern lookup. 
 */
public class Ydict<E> implements Dictionary<char[], E> {
	/** flag indicating whether to use new pattern/intersection algorithm */
	private static final boolean USE_NEW_INTERSECTION_ALGORITHM = true;

	/**
	 * buckets organized by word length
	 * each element[N] contains all words of length N
	 */
	private List<Map<String, E>> wordsOfLength = new ArrayList<Map<String, E>>();

	private Pair<char[], E>[][] wordsOfLengthArray;

	/**
	 * lenBuckets_ stored as [lengthOfWord][indexOfCharacterInWord 0..lengthOfWord - 1][character 0..25][N where N is the # of selections]
	 * each one of these elements stores an index into wordsOfLengthArray[lengthOfWord] to identify the particular word
	 */ 
	// lenBuckets[w][x][y][z]
	// where w == length of word
	//       x == indexOfCharacterInWord (0..lengthOfWord - 1)
	//       y == character (0..25)
	//       z ranges over final array of indexes into wordsOfLengthArray[w] to identify the particular word
	private int[][][][] lenBuckets;

	// insert words into wordsOfLength_ buckets initially
	public void insert(char[] word, E entry) {
		while (wordsOfLength.size() <= word.length)
			wordsOfLength.add(new HashMap<String, E>());
		// This next line removes duplicates, always using latest version
		wordsOfLength.get(word.length).put(new String(word), entry);
	}

	@SuppressWarnings("unchecked")
	public void rebalance() {
		wordsOfLengthArray = new Pair[wordsOfLength.size()][];
		lenBuckets = new int[wordsOfLength.size()][][][];
		for (int i = 0; i < wordsOfLengthArray.length; ++i) {
			Map<String, E> wordsOfLengthMap = wordsOfLength.get(i);
			wordsOfLength.set(i, null);
			wordsOfLengthArray[i] = new Pair[wordsOfLengthMap.size()];
			int index = 0;
			List<Integer>[][] aToZ = new List[i][26];
			for (int k = 0; k < i; ++k)
				for (int j = 0; j < 26; ++j)
					aToZ[k][j] = new ArrayList<Integer>();

			List<Map.Entry<String, E>> entryList = new ArrayList<Map.Entry<String, E>>(wordsOfLengthMap.entrySet());
			Collections.shuffle(entryList);
			for (Map.Entry<String, E> wordOfLengthEntry : entryList) {
				char[] chars = wordOfLengthEntry.getKey().toCharArray();
				for (int j = 0; j < chars.length; ++j)
					aToZ[j][chars[j] - 'A'].add(index);
				wordsOfLengthArray[i][index++] = new Pair<char[], E>(chars, wordOfLengthEntry.getValue());
			}

			lenBuckets[i] = new int[i][][];

			// Now create lenBuckets_
			for (int letterIndex = 0; letterIndex < i; ++letterIndex) {
				lenBuckets[i][letterIndex] = new int[26][];
				for (int j = 0; j < 26; ++j) {
					int size = aToZ[letterIndex][j].size();
					lenBuckets[i][letterIndex][j] = new int[size];
					for (int k = 0; k < size; ++k)
						lenBuckets[i][letterIndex][j][k] = aToZ[letterIndex][j].get(k);
				}
			}
		}

		// Free up original wordsOfLength_ storage
		wordsOfLength = null;
	}


	public void bulkInsert(List<Pair<char[], E>> entries) {
		for (Pair<char[], E> entry : entries)
			insert(entry.first, entry.second);
	}

	public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		int len = pattern.length;
		if (lenBuckets.length <= len)
			return Collections.emptyList();
		int[][][] buckets = lenBuckets[len];

		if (USE_NEW_INTERSECTION_ALGORITHM) {
			// Use Svs + Galloping Search, as suggested by "Faster Set Intersection Algorithms for Text Searching"
			int[][] wordLists = new int[pattern.length][];
			//TODO investigate whether something like QuickSort might work better than this insertion sort here
			int wordListsSize = 0;
			for (int index = 0; index < len; ++index) {
				char c = pattern[index];
				if (c != WILDCARD) {
					addWordListInOrderByCardinality(wordLists, wordListsSize, buckets[index][c - 'A']);
					++wordListsSize;
				}
			}
			// If all wildcards, return entire bucketful
			if (wordListsSize == 0)
				return Arrays.asList(wordsOfLengthArray[len]);

			// Need to copy wordLists[0] to candidateSet so we can mutate it w/o corrupting Dictionary
			int[] candidateSet = new int[wordLists[0].length];
			int[] oldCandidateSet = new int[wordLists[0].length];

			int candidateSetSize = wordLists[0].length;
			int oldCandidateSetSize = 0;
			System.arraycopy(wordLists[0], 0, candidateSet, 0, candidateSetSize);
			for (int i = 1; i < wordListsSize; ++i) {
				int[] setToCheck = wordLists[i];

				// swap oldCandidate <-> candidate, and clear candidateSetSize
				oldCandidateSetSize = candidateSetSize;
				candidateSetSize = 0;
				int[] temp = oldCandidateSet;
				oldCandidateSet = candidateSet;
				candidateSet = temp;

				int probe = 1;
				for (int j = 0; j < oldCandidateSetSize; ++j) {
					int key = oldCandidateSet[j];
					// Galloping search:
					// Probe at 1, 3, 7, 15, .. until found element greater than key
					// then do binary search in the last interval from prev probe to probe - 1
					while (probe < setToCheck.length && setToCheck[probe] <= key)
						probe = (probe << 1) + 1;
					int low = probe >> 1;
					int high = probe >= setToCheck.length ? setToCheck.length - 1 : probe - 1;

					// regular binary search on [low, high]
					while (low <= high) {
						int mid = (low + high) >> 1;
						int midVal = setToCheck[mid];
						if (midVal < key)
							low = mid + 1;
						else if (midVal > key)
							high = mid - 1;
						else {
							candidateSet[candidateSetSize++] = key;
							break;
						}
					}
				}
			}

			List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(candidateSetSize);
			Pair<char[], E>[] arr = wordsOfLengthArray[len];
			for (int i = 0; i < candidateSetSize; ++i)
				retval.add(arr[candidateSet[i]]);
			return retval;
		} else {
			// Two possibilities: all wildcards, in which case we use full entry from wordsOfLengthArray
			// Or at least 1 non-wildcard, in which case we use the lenBuckets intersections
			int[] currIntersection = null;
			int[] oldIntersection = null;
			int[] temp = null;
			int currIntersectionSize = -1;
			for (int index = 0; index < len /* && currIntersectionSize != 0 */; ++index) {
				char c = pattern[index];
				if (c != WILDCARD) {
					int[] words = buckets[index][c - 'A'];
					if (currIntersection == null) {
						currIntersection = words;
						currIntersectionSize = words.length;
					} else if (oldIntersection == null) {
						oldIntersection = currIntersection;
						currIntersection = new int[words.length];
						currIntersectionSize = getIntersection(oldIntersection, currIntersectionSize, words, words.length, currIntersection);
						oldIntersection = new int[words.length];
					} else {
						// Swap old/curr intersection buffers
						temp = oldIntersection;
						oldIntersection = currIntersection;
						currIntersection = temp;
						currIntersectionSize = getIntersection(oldIntersection, currIntersectionSize, words, words.length, currIntersection);
					}
				}
			}

			if (currIntersection == null)
				// All wildcards -- use entire bucketfull
				return Arrays.asList(wordsOfLengthArray[len]);

			List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(currIntersectionSize);
			for (int i = 0; i < currIntersectionSize; ++i)
				retval.add(wordsOfLengthArray[len][currIntersection[i]]);
			return retval;
		}
	}

	public E lookup(char[] key) {
		if (lenBuckets.length <= key.length)
			return null;

		List<Pair<char[], E>> patternMatches = getPatternMatches(key);
		if (patternMatches.size() == 0)
			return null;
		if (patternMatches.size() > 1)
			throw new IllegalArgumentException("Got " + patternMatches.size() + " matches for " + new String(key));
		return patternMatches.get(0).second;
	}

	public boolean isPatternInDictionary(char[] pattern) {
		return getPatternMatches(pattern).size() > 0;
	}

	/**
	 * Return resettable iterator over entries in the dictionary matching the specified pattern.
	 * @param pattern pattern to match
	 * @return resettable iterator over entries in the dictionary matching the specified pattern
	 */
	public ResettableIterator<Pair<char[], E>> getIterator(char[] pattern) {
		return new YdictIterator<E>(getPatternMatches(pattern));
	}

	// store the N elements of intersection of a and b into first N elements of result, and return N
	// assumes a and b are both sorted in ascending order
	private int getIntersection(int[] a, int numUsedInA, int[] b, int numUsedInB, int[] result) {
		if (numUsedInA == 0 || numUsedInB == 0)
			return 0;
		int aIndex = 0;
		int bIndex = 0;
		int rIndex = 0;
		int aElem = a[aIndex++];
		int bElem = b[bIndex++];
		while (true) {
			if (aElem < bElem) {
				if (aIndex >= numUsedInA)
					return rIndex;
				aElem = a[aIndex++];
			} else if (aElem > bElem) {
				if (bIndex >= numUsedInB)
					return rIndex;
				bElem = b[bIndex++];
			} else /*if (aElem == bElem)*/ {
				result[rIndex++] = aElem;
				if (aIndex >= numUsedInA || bIndex >= numUsedInB)
					return rIndex;
				aElem = a[aIndex++];
				bElem = b[bIndex++];
			}
		}
	}

	// put words into wordLists in order of its cardinality (length)
	private void addWordListInOrderByCardinality(int[][] wordLists, int wordListsSize, int[] words) {
		int index = 0;
		while (index < wordListsSize && wordLists[index].length <= words.length)
			++index;
		// move any remaining elements forward to make room to stick in words
		for (int i = wordListsSize - 1; i >= index; --i)
			wordLists[i + 1] = wordLists[i];
		wordLists[index] = words;
	}

	private static class YdictIterator<E> implements ResettableIterator<Pair<char[], E>> {
		/** list of entries to iterate over */
		private final List<Pair<char[], E>> entries;

		/** pointer to next entry to return */
		private int nextPointer;

		public YdictIterator(List<Pair<char[], E>> entries) {
			this.entries = entries;
			reset();
		}

		/**
		 * Reset iterator back to its initial creation state.
		 */
		public void reset() {
			nextPointer = 0;
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
			return nextPointer < entries.size();
		}

		public Pair<char[], E> next() {
			Pair<char[], E> retval = peekNext();
			++nextPointer;
			return retval;
		}

		/** just like next but without advancing iterator */
		public Pair<char[], E> peekNext() {
			return entries.get(nextPointer);
		}
	}
}

