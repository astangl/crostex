/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.ResettableIterator;


/**
 * Implementation of dictionary optimized for crossword pattern lookup. 
 * @deprecated  According to DictionariesTest, this Dictionary is not correct!!!
 * NOTE: According to DictionariesTest, this Dictionary is not correct!!!
 */
public class Xdict<E> implements Dictionary<char[], E> {
    /**
     * buckets organized by word length.
     * Element[N] logically points to all words of length N
     * A couple elements end up unused, but it saves subtractions.
     * Element[N] really points to an array of length N. Each element
     * in that array points to an array of length 26, for each letter
     * that can appear in that position in the word.
     * 
     * Each element in this final array references a list of words of
     * the specified length that contain the specified letter in the
     * specified position.
     *
     * len bucket -> letter slots -> A-Z array -> words
     */
    private List<List<?>[][]> lenBuckets = new ArrayList<List<?>[][]>();

    /** private scratchpad */
    private char[] scratchpad = new char[100];

    private void growLenList() {
        int nextSize = lenBuckets.size();
        List<?>[][] letterSlots = new List<?>[nextSize][26];
        for (int i = 0; i < nextSize; ++i) {
//            List<?>[] atozArray = new List<?>[26];
            for (int j = 0; j < 26; ++j)
//                atozArray[i][j] = new ArrayList<E>();
                letterSlots[i][j] = new ArrayList<Pair<char[], E>>();
//            letterSlots[i] = atozArray;
        }

        lenBuckets.add(letterSlots);
    }

    public void rebalance() {}
    
    public void bulkInsert(List<Pair<char[], E>> entries) {
		for (Pair<char[], E> entry : entries)
			insert(entry.first, entry.second);
	}

	public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(200);
		for (ResettableIterator<Pair<char[], E>> it = getIterator(pattern); it.hasNext(); )
			retval.add(it.next());
		return retval;
	}

    public E lookup(char[] key) {
        if (lenBuckets.size() <= key.length)
            return null;

        List[][] letterSlots = lenBuckets.get(key.length);
        int[] freqs = getFreqDist(key, letterSlots);
        if (freqs.length != key.length)
            throw new IllegalArgumentException("Illegal key value '" + new String(key) + "' passed to findKey");
        List<Pair<char[], E>> list = letterSlots[freqs[0]][key[freqs[0]] - 'A'];
candidates:
        for (Pair<char[], E> candidate : list) {
            char[] text = candidate.first;
            
            for (int i = 1; i < freqs.length; ++i) {
                int freqIndex = freqs[i];
                if (text[freqIndex] != key[freqIndex])
                    continue candidates;
            }

            return candidate.second;
        }
        return null;
    }

    public boolean isPatternInDictionary(char[] pattern) {
        if (lenBuckets.size() <= pattern.length)
            return false;
        
        return getIterator(pattern).hasNext();
    }

    // return list of zero-based indexes into word, pointing at
    // non-wildcard characters, in increasing order of freq. in dict
    private int[] getFreqDist(char[] word, List[][] letterSlots) {
        // Count # of non-wildcard chars in word to determine size of retval
        int retsize = 0;
//        char[] temp = new char[word.length]
        for (int i = 0; i < word.length; ++i) {
            scratchpad[i] = word[i];
            if (word[i] != WILDCARD)
                ++retsize;
        }

        // Enumerate non-wildcard char positions in increasing order of freq
        int[] retval = new int[retsize];
        for (int index = 0; index < retsize; ++index) {
            int lowestSoFar = Integer.MAX_VALUE;
            int lowestSoFarIndex = -1;
            for (int i = 0; i < word.length; ++i) {
                if (scratchpad[i] != WILDCARD) {
                    int atoz = scratchpad[i] - 'A';
                    int nMatching = letterSlots[i][atoz].size();
                    if (nMatching < lowestSoFar) {
                        lowestSoFar = nMatching;
                        lowestSoFarIndex = i;
                    }
                }
            }
            retval[index] = lowestSoFarIndex;
            // Change char to wildcard so we ignore it from now on
            scratchpad[lowestSoFarIndex] = WILDCARD;
        }
        return retval;
    }

    public void insert(char[] word, E entry) {
        while (lenBuckets.size() <= word.length)
            growLenList();

        List[][] letterSlots = lenBuckets.get(word.length);
        Pair<char[], E> pair = new Pair<char[], E>(word, entry);
        for (int i = 0; i < word.length; ++i) {
            char c = word[i];
            if (c < 'A' || c > 'Z')
                throw new IllegalArgumentException("Attempt to insert word '" + new String(word) + "' which has chars not in A-Z.");
            int index = c - 'A';
            letterSlots[i][index].add(pair);
        }
    }

	public ResettableIterator<Pair<char[], E>> getIterator(char[] pattern) {
		return new XdictIterator(pattern);
	}

    private class XdictIterator implements ResettableIterator<Pair<char[], E>>
    {
        /** indexes by freq dist of non-wildcard chars; empty if all wildcards */
        private int[] freqs;

        /** list of candidates to check */
        private List<Pair<char[], E>> candidates;

        /** pattern */
        private final char[] pattern;

        /** index of next element to check in list */
        private int nextListIndex;

        /** letter slots */
        private final List[][] letterSlots;

        /** flag indicating whether pattern is all wildcards */
        private final boolean allWildcards;

        /** index of which list we are iterating over (as opposed to nextListIndex which is index INSIDE list), only used for all wildcard iterating */
        private int indexOfList;

        /** next element to return, or null if none available */
        private Pair<char[], E> next;

	    /** last key returned */
	    private char[] lastKeyReturned;
	    
	    /**
	     * characters to exclude from regexp matching, based upon position.
	     * exclusions_[N] corresponds to pattern[N], and is really only useful when pattern[N] is a wildcard
	     * exclusions_ elements are each bitmaps, with 1 corresponding to A and 1 << 25 corresponding to Z
	     * If the corresponding exclusions bit is set, that letter should be avoided at that wildcard position
	     */
	    private int[] exclusions;

        public XdictIterator(char[] pattern) {
            //TODO if client mutates pattern, it will mess us up unless we copy it
            this.pattern = pattern;

            // figure out list to iterate over and desc freqs
            if (lenBuckets.size() <= pattern.length) {
                candidates = Collections.emptyList();
                letterSlots = null;
                allWildcards = false;
                return;
            }

            letterSlots = lenBuckets.get(pattern.length);
            freqs = getFreqDist(pattern, letterSlots);

            if (freqs.length == 0) {
                // Handle all wildcards special
                allWildcards = true;
            } else {
                // We have at least 1 non-wildcard char
                allWildcards = false;
                candidates = letterSlots[freqs[0]][pattern[freqs[0]] - 'A'];
            }
            reset();
        }

	    /**
	     * @see us.stangl.crostex.util.ResettableIterator#avoidLetterAt(int)
	     */
	    public void avoidLetterAt(int index) {
	    	// Grow exclusions_ if necessary
	    	if (exclusions.length <= index) {
	    		int[] newArray = new int[index + 1];
	    		for (int i = 0; i < exclusions.length; ++i)
	    			newArray[i] = exclusions[i];
	    		exclusions = newArray;
	    	}
	    	char badChar = lastKeyReturned[index];
	    	exclusions[index] |= 1 << (badChar - 'A');

	    	// Check to see if next one to return violates the new avoidance constraint
	    	if (! hasNext())
	    		return;
	    	Pair<char[], E> nextToReturn = peekNext();

	    	if (nextToReturn.first[index] == badChar)
	    		findNext();
	    }

	    public void remove() {
	        throw new UnsupportedOperationException("remove not implemented");
	    }

        public boolean hasNext() {
            return next != null;
        }
        
	    public Pair<char[], E> next() {
	        Pair<char[], E> retval = peekNext();
	        findNext();
	        return retval;
	    }
	    
        public Pair<char[], E> peekNext() {
        	lastKeyReturned = next.first;
        	return next;
        }

        public void reset() {
            if (allWildcards) {
                indexOfList = 0;
                candidates = letterSlots[0][indexOfList];
            }
            nextListIndex = 0;
	        exclusions = new int[0];
	        lastKeyReturned = new char[pattern.length];

            findNext();
        }

        private void findNext() {
            next = findNextMatchingFromList();
            if (next != null || !allWildcards)
                return;

            while (indexOfList < 25) {
                candidates = letterSlots[0][++indexOfList];
                next = findNextMatchingFromList();
                if (next != null)
                    return;
            }
        }

        // find next matching candidate from list, if any, else null
        private Pair<char[], E> findNextMatchingFromList() {
candidates:
            while (nextListIndex < candidates.size()) {
                Pair<char[], E> candidate = candidates.get(nextListIndex++);
                char[] text = candidate.first;
                
                for (int i = 1; i < freqs.length; ++i) {
                    int freqIndex = freqs[i];
                    if (text[freqIndex] != pattern[freqIndex])
                        continue candidates;
                }

                return candidate;
            }
            return null;
        }
    }
}

