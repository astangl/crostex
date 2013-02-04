/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import us.stangl.crostex.util.ResettableIterator;


/**
 * Implementation of dictionary optimized for crossword pattern lookup. 
 */
public class Ydict<E> implements Dictionary<char[], E> {
	/** flag indicating whether to use new pattern/intersection algorithm */
	private static final boolean USE_NEW_INTERSECTION_ALGORITHM = false;
	
    /** wildcard character */
    public static final char WILDCARD = '_';

    /**
     * buckets organized by word length
     * each element[N] contains all words of length N
     */
    private List<Map<String, E>> wordsOfLength_ = new ArrayList<Map<String, E>>();
    
    private Pair<char[], E>[][] wordsOfLengthArray_;
    
    /**
     * lenBuckets_ stored as [lengthOfWord][indexOfCharacterInWord 0..lengthOfWord - 1][character 0..25][N where N is the # of selections]
     * each one of these elements stores an index into wordsOfLengthArray[lengthOfWord] to identify the particular word
     */ 
    private int[][][][] lenBuckets_;
    
    // insert words into wordsOfLength_ buckets initially
    public void insert(char[] word, E entry) {
        while (wordsOfLength_.size() <= word.length)
        	wordsOfLength_.add(new HashMap<String, E>());
        wordsOfLength_.get(word.length).put(new String(word), entry);
    }

    public void rebalance() {
    	wordsOfLengthArray_ = new Pair[wordsOfLength_.size()][];
    	lenBuckets_ = new int[wordsOfLength_.size()][][][];
    	for (int i = 0; i < wordsOfLengthArray_.length; ++i) {
    		Map<String, E> wordsOfLength = wordsOfLength_.get(i);
    		wordsOfLength_.set(i, null);
    		wordsOfLengthArray_[i] = new Pair[wordsOfLength.size()];
    		int index = 0;
			List<Integer>[][] aToZ = new List[i][26];
			for (int k = 0; k < i; ++k)
				for (int j = 0; j < 26; ++j)
					aToZ[k][j] = new ArrayList();
			
			List<Map.Entry<String, E>> entryList = new ArrayList<Map.Entry<String, E>>(wordsOfLength.entrySet());
			Collections.shuffle(entryList);
//			for (Map.Entry<String, E> wordOfLengthEntry : wordsOfLength.entrySet()) {
			for (Map.Entry<String, E> wordOfLengthEntry : entryList) {
    			char[] chars = wordOfLengthEntry.getKey().toCharArray();
    			for (int j = 0; j < chars.length; ++j)
    				aToZ[j][chars[j] - 'A'].add(index);
    			wordsOfLengthArray_[i][index++] = new Pair<char[], E>(chars, wordOfLengthEntry.getValue());
    		}
    		
    		lenBuckets_[i] = new int[i][][];
    		
    		// Now create lenBuckets_
    		for (int letterIndex = 0; letterIndex < i; ++letterIndex) {
    			lenBuckets_[i][letterIndex] = new int[26][];
	    		for (int j = 0; j < 26; ++j) {
	    			int size = aToZ[letterIndex][j].size();
	    			lenBuckets_[i][letterIndex][j] = new int[size];
	    			for (int k = 0; k < size; ++k)
	    				lenBuckets_[i][letterIndex][j][k] = aToZ[letterIndex][j].get(k);
//System.out.println("lenBuckets_[" + i + "][" + letterIndex + "][" + j + "].length = " + size);
	    		}
    		}
    	}
    	
    	// Free up original wordsOfLength_ storage
    	wordsOfLength_ = null;
    }

    // store N elements of intersection of a and b into first N elements of result, and return N
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
    
    
    public void bulkInsert(List<Pair<char[], E>> entries) {
		for (Pair<char[], E> entry : entries)
			insert(entry.first_, entry.second_);
	}

    /** put words into wordLists in order of its cardinality (length) and return new fillPointer (# used in wordLists) */
    private int addWordListInOrderByCardinality(int[][] wordLists, int fillPointer, int[] words) {
    	int index = 0;
    	while (index < fillPointer && wordLists[index].length <= words.length)
    		++index;
    	// move any remaining elements forward to make room to stick in words
    	for (int i = fillPointer - 1; i >= index; --i)
    		wordLists[i + 1] = wordLists[i];
    	wordLists[index] = words;
    	return fillPointer + 1;
    }
    
    public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		int len = pattern.length;
        if (lenBuckets_.length <= len)
            return Collections.EMPTY_LIST;
		int[][][] buckets = lenBuckets_[len];
		
		if (USE_NEW_INTERSECTION_ALGORITHM) {
			// Use Svs + Galloping Search, as suggested by "Faster Set Intersection Algorithms for Text Searching"
			int index = 0;
			int[][] wordLists = new int[pattern.length][];
			int fillPointer = 0;
			while (index < len) {
				char c = pattern[index];
				if (c != WILDCARD) {
					fillPointer = addWordListInOrderByCardinality(wordLists, fillPointer, buckets[index][c - 'A']);
				}
				++index;
			}
			// If all wildcards, return entire bucketful
			if (fillPointer == 0)
				return Arrays.asList(wordsOfLengthArray_[len]);
			
			//TODO see if we need to actually have special-case handling for fillPointer == 1
	//		if (fillPointer == 1)
	//			return Arrays.asList(wordLists[0]);
			
			int[] candidateSet = new int[wordLists[0].length];
			int[] oldCandidateSet = new int[wordLists[0].length];
			int[] highwaterMarks = new int[fillPointer];			// high-water mark into each set, initialized to 1
			for (int i = 0; i < fillPointer; ++i)
				highwaterMarks[i] = 1;
			
			int candidateSetSize = wordLists[0].length;
			int oldCandidateSetSize = 0;
			for (int i = 0; i < candidateSetSize; ++i)
				candidateSet[i] = wordLists[0][i];
			for (int i = 1; i < fillPointer; ++i) {
				int[] setToCheck = wordLists[i];
				
				// swap oldCandidate <-> candidate, and clear candidateSetSize
				oldCandidateSetSize = candidateSetSize;
				candidateSetSize = 0;
				int[] temp = oldCandidateSet;
				oldCandidateSet = candidateSet;
				candidateSet = temp;
				
				for (int j = 0; j < oldCandidateSetSize; ++j) {
					int key = oldCandidateSet[j];
					// Galloping search:
					// Use low to probe at 1, 3, 7, 15, .. until found element greater than key
					// then do binary search in the last interval from prev probe to probe - 1
					int low = highwaterMarks[i];
					while (low < setToCheck.length && setToCheck[low] <= key)
						low = low << 1 + 1;
					highwaterMarks[i] = low;
					int high = (low >= setToCheck.length) ? setToCheck.length - 1 : low - 1;
					low >>= 1;
	
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
			for (int i = 0; i < candidateSetSize; ++i)
				retval.add(wordsOfLengthArray_[len][candidateSet[i]]);
			return retval;
		} else {
			// Two possibilities: all wildcards, in which case we use full entry from wordsOfLengthArray
			// Or at least 1 non-wildcard, in which case we use the lenBuckets intersections
			int index = 0;
			int[] currIntersection = null;
			int[] oldIntersection = null;
			int[] temp = null;
			int currIntersectionSize = -1;
			while (index < len /*&& currIntersectionSize != 0*/) {
				char c = pattern[index];
				if (c != WILDCARD) {
					int[] words = buckets[index][c - 'A'];
					if (currIntersection == null) {
						currIntersection = words;
	//					currIntersection = new int[words.length];
	//					oldIntersection = new int[words.length];
	//					for (int i = 0; i < words.length; ++i)
	//						currIntersection[i] = words[i];
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
	//ystem.out.println("index = " + index + ", words.length = " + words.length + ", currIntersectionSize = " + currIntersectionSize);				
				}
				++index;
			}
	
	//System.out.println("returning from getPatternMatches");
			if (currIntersection == null)
				// All wildcards -- use entire bucketfull
				return Arrays.asList(wordsOfLengthArray_[len]);
	
			List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(currIntersectionSize);
			for (int i = 0; i < currIntersectionSize; ++i)
				retval.add(wordsOfLengthArray_[len][currIntersection[i]]);
			return retval;
		}
	}

    public E lookup(char[] key) {
        if (lenBuckets_.length <= key.length)
            return null;

    	List<Pair<char[], E>> patternMatches = getPatternMatches(key);
    	if (patternMatches.size() == 0)
    		return null;
    	if (patternMatches.size() > 1)
    		throw new IllegalArgumentException("Got " + patternMatches.size() + " matches for " + new String(key));
    	return patternMatches.get(0).second_;
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

	private static class YdictIterator<E> implements ResettableIterator<Pair<char[], E>> {
		/** list of entries to iterate over */
		private final List<Pair<char[], E>> entries_;

		/** pointer to next entry to return */
		private int nextPointer_;

		public YdictIterator(List<Pair<char[], E>> entries) {
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

