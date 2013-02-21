/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.dictionary;

import java.util.ArrayList;
import java.util.List;

import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.ResettableIterator;

/**
 * Ternary search tree implementation of Dictionary.
 */
public class TST<E> implements Dictionary<char[], E> {
	/** flag indicating whether to include avoidance logic */
	private static final boolean INCLUDE_AVOID = false;
	
	/** flag to enable additional debug code */
	private static final boolean DEBUG = false;
	
	/** each element at index N in forest_ contains an array of TST heads for words of size N + 3 */
	private List<TstNode<E>[]> forest = new ArrayList<TstNode<E>[]>();
	
//	private TstNode<E>[] heads_ = new TstNode[26];
	
	public List<Pair<char[], E>> getPatternMatches(char[] pattern) {
		List<Pair<char[], E>> retval = new ArrayList<Pair<char[], E>>(200);
		for (ResettableIterator<Pair<char[], E>> it = getIterator(pattern); it.hasNext(); )
			retval.add(it.next());
		return retval;
	}

	/**
	 * Return resettable iterator over entries in the dictionary matching the specified pattern.
	 * @param pattern pattern to match
	 * @return resettable iterator over entries in the dictionary matching the specified pattern
	 */
	public ResettableIterator<Pair<char[], E>> getIterator(char[] pattern) {
		int forestIndex = pattern.length - 3;
		growForestToSize(forestIndex + 1);
		return new TstIterator<E>(forest.get(forestIndex), pattern);
//		return new TstIterator<E>(heads_, pattern);
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
//System.out.println("adding " + key);		
		int forestIndex = key.length - 3;
		growForestToSize(forestIndex + 1);
		TstNode<E>[] heads = forest.get(forestIndex);
		heads[headIndex] = add(heads[headIndex], key, 1, entry);
//		heads_[headIndex] = add(heads_[headIndex], key, 1, entry);
	}

	/**
	 * Lookup key in dictionary, returning its associated entry, if found, else null.
	 * Assumes length is at least 1.
	 * @param key key
	 * @return Entry associated with key if it is found, else null
	 */
	public E lookup(char[] key) {
		int forestIndex = key.length - 3;
		growForestToSize(forestIndex + 1);
		TstNode<E>[] heads = forest.get(forestIndex);
		return lookup(heads[key[0] - 'A'], key, 1);
	}

	/**
	 * Return whether the specified pattern has any entries in this dictionary.
	 * @param pattern
	 * @return true if the pattern has at least 1 matching key in the dictionary
	 */
	public boolean isPatternInDictionary(char[] pattern) {
		int forestIndex = pattern.length - 3;
		growForestToSize(forestIndex + 1);
		TstNode<E>[] heads = forest.get(forestIndex);
		if (pattern[0] != WILDCARD)
			return isPatternFound(heads[pattern[0] - 'A'], pattern, 1);

		for (TstNode<E> head : heads)
			if (isPatternFound(head, pattern, 1))
				return true;
		return false;
	}

	public String toString() {
		StringBuilder retval = new StringBuilder();
		retval.append("{");
		for (int j = 0; j < forest.size(); ++j) {
			TstNode<E>[] heads = forest.get(j);
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

    public void rebalance() {}
    
	private void growForestToSize(int size) {
		while (forest.size() < size)
			forest.add(new TstNode[26]);
	}

	private boolean isPatternFound(TstNode<E> node, char[] key, int keyIndex) {
		if (node == null)
			return false;
		char c = key[keyIndex];
		if ((c == WILDCARD || c < node.splitChar) && isPatternFound(node.leftChild, key, keyIndex))
			return true;
		if (c == WILDCARD || c == node.splitChar) {
			if (keyIndex < key.length - 1) {
				if (isPatternFound((TstNode<E>)node.middleChild, key, keyIndex + 1))
					return true;
			} else if (node.middleChild != null) {
				return true;
			}
		}
		return ((c == WILDCARD || c > node.splitChar) && isPatternFound(node.rightChild, key, keyIndex));
	}
	
	private E lookup(TstNode<E> node, char[] key, int keyIndex) {
		if (node == null)
			return null;
		char c = key[keyIndex];
		if (c < node.splitChar)
			return lookup(node.leftChild, key, keyIndex);
		if (c > node.splitChar)
			return lookup(node.rightChild, key, keyIndex);
		if (keyIndex < key.length - 1)
			return lookup((TstNode<E>)node.middleChild, key, keyIndex + 1);
		return (E)node.middleChild;
	}
	
	/** recursively insert middle entry from subarray, then left and right subarrays to get balanced TST */
	private void bulkInsert(List<Pair<char[], E>> entries, int lowIndex, int highIndex) {
		if (lowIndex > highIndex)
			return;						// we're done with this branch
		
		// First insert middle element from our subarray...
		int middleIndex = (highIndex + lowIndex) / 2;
		Pair<char[], E> middleEntry = entries.get(middleIndex);
		insert(middleEntry.first, middleEntry.second);

		// ...now recursively do left and right subarrays
		bulkInsert(entries, lowIndex, middleIndex - 1);
		bulkInsert(entries, middleIndex + 1, highIndex);
	}

	private TstNode<E> add(TstNode<E> node, char[] key, int keyIndex, E entry) {
		char c = key[keyIndex];
		if (node == null)
			node = new TstNode<E>(c);
		if (c < node.splitChar)
			node.leftChild = add(node.leftChild, key, keyIndex, entry);
		else if (c > node.splitChar)
			node.rightChild = add(node.rightChild, key, keyIndex, entry);
		else if (keyIndex < key.length - 1)
			node.middleChild = add((TstNode<E>)node.middleChild, key, keyIndex + 1, entry);
		else
			node.middleChild = entry;
		return node;
	}
	
	private static class TstNode<E> {
		/** split character */
		private final char splitChar;
		
		/** left child contains all children whose curr. char is less than our split char */
		private TstNode<E> leftChild;
		
		/** 
		 * in interior node, middle child contains all children whose curr. char is our split char
		 * in leaf node, middle child points to entry
		 */
		private Object middleChild;
		
		/** right child contains all children whose curr. char is greater than our split char */
		private TstNode<E> rightChild;
		
		public TstNode(char splitChar) {
			this.splitChar = splitChar;
		}

		public String toString() {
			StringBuilder retval = new StringBuilder();
//			String entryString = entry_ == null ? "null" : entry_.toString();
			String leftChildString = leftChild == null ? "null" : leftChild.toString();
			String middleChildString = middleChild == null ? "null" : middleChild.toString();
			String rightChildString = rightChild == null ? "null" : rightChild.toString();
			retval.append("{").append("splitChar = ").append(splitChar)
//				.append(", entry_ = ").append(entryString)
				.append(", leftChild = ").append(leftChildString)
				.append(", middleChild = ").append(middleChildString)
				.append(", rightChild = ").append(rightChildString)
				.append("}");
			return retval.toString();
		}
	}

	
	
	
	private static class TstIterator<E> implements ResettableIterator<Pair<char[], E>> {
	    /** pattern */
	    private final char[] pattern;
	    
	    /** array of TST heads */
	    private final TstNode<E>[] heads;

	    /** index of which head we are currently using */
	    private int headIndex;
	    
	    /** last key returned */
	    private char[] lastKeyReturned;
	    
	    /**
	     * characters to exclude from regexp matching, based upon position.
	     * exclusions_[N] corresponds to pattern[N], and is really only useful when pattern[N] is a wildcard
	     * exclusions_ elements are each bitmaps, with 1 corresponding to A and 1 << 25 corresponding to Z
	     * If the corresponding exclusions bit is set, that letter should be avoided at that wildcard position
	     */
	    private int[] exclusions;

	    enum Pointer { LEFT, MIDDLE, RIGHT };
	    /**
	     * list of pointers showing what type of pointer was used to arrive each corresponding node
	     */
	    private Pointer[] typeOfPointerToNode = new Pointer[32];
//	    private List<Pointer> typeOfPointerToNode_ = new ArrayList<Pointer>(32);

	    /**
	     * list of Nodes showing the current descent all the way to current pos
	     * empty if there is no next (really current since we always point to next)
	     */
	    private TstNode<E>[] descentGraph = new TstNode[32];
	    
	    // fill pointer, showing next element in typeOfPointerToNode and descentGraph to write to
	    // everything from 0 .. fillPointer considered valid
	    private int fillPointer = 0;
	    
//	    private List<TstNode<E>> descentGraph_ = new ArrayList<TstNode<E>>(32);

	    public TstIterator(TstNode<E>[] heads, char[] pattern) {
	        this.pattern = pattern;
	        this.heads = heads;

	        reset();
	    }

	    /**
	     * Reset iterator back to its initial creation state.
	     */
	    public void reset() {
	    	headIndex = pattern[0] == WILDCARD ? 0 : pattern[0] - 'A';
	    	fillPointer = 0;
//	        descentGraph_.clear();
//	        typeOfPointerToNode_.clear();
	        lastKeyReturned = new char[pattern.length];
	        if (INCLUDE_AVOID)
	        	exclusions = new int[0];
	        findFirst();
	    }

	    /**
	     * @see us.stangl.crostex.util.ResettableIterator#avoidLetterAt(int)
	     */
	    public void avoidLetterAt(int index) {
	    	if (INCLUDE_AVOID) {
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
	    }

	    public void remove() {
	        throw new UnsupportedOperationException("remove not implemented");
	    }

	    /**
	     * @return whether any elements are remaining (i.e, safe to call next).
	     */
	    public boolean hasNext() {
	        return fillPointer > 0;
	    }

	    public Pair<char[], E> next() {
	        Pair<char[], E> retval = peekNext();
	        findNext();
	        return retval;
	    }
	    
	    /** just like next but without advancing iterator */
	    public Pair<char[], E> peekNext() {
	    	lastKeyReturned[0] = (char)(headIndex + 'A');
  	        int index = 1;
  	        for (int i = 0; i < fillPointer - 1; ++i) {
  	        	if (typeOfPointerToNode[i + 1] == Pointer.MIDDLE)
  	        		lastKeyReturned[index++] = descentGraph[i].splitChar;
  	        }
  	        TstNode<E> endNode = descentGraph[fillPointer - 1];
  	        lastKeyReturned[index++] = endNode.splitChar;
  	        if (DEBUG) {
	  	        if (index != lastKeyReturned.length) {
	  	        	throw new RuntimeException("index incorrectly calculated, is " + index + ", rather than " + lastKeyReturned.length);
	  	        }
  	        }
  	        
  	        return new Pair<char[], E>(lastKeyReturned, (E)endNode.middleChild);
	    }

	    private void addNodeAndPointer(TstNode<E> node, Pointer pointer) {
	    	int currLen = descentGraph.length;
	    	if (fillPointer == currLen) {
	    		int newLen = currLen * 2;
	    		// allocate new node and pointer arrays, twice as big and copy old contents in
	    		TstNode<E>[] newNodeArray = new TstNode[newLen];
	    		Pointer[] newPointerArray = new Pointer[newLen];
	    		for (int i = 0; i < currLen; ++i) {
	    			newNodeArray[i] = descentGraph[i];
	    			newPointerArray[i] = typeOfPointerToNode[i];
	    		}
	    		descentGraph = newNodeArray;
	    		typeOfPointerToNode = newPointerArray;
	    	}
	    	
	    	descentGraph[fillPointer] = node;
	    	typeOfPointerToNode[fillPointer++] = pointer;
	    }
	    
	    private boolean findFirst() {
	        if (pattern[0] == WILDCARD) {
	        	
	        	if (INCLUDE_AVOID) {
		        	// Skip heads we have been warned to avoid
		        	int headMask = 1;	// for A
		        	int headsToAvoid = exclusions.length == 0 ? 0 : exclusions[0];
	//	        	Set headsToAvoid = exclusionList_.size() == 0 ? Collections.EMPTY_SET : exclusionList_.get(0);
		            for (headIndex = 0; headIndex < heads.length; ++headIndex) {
		            	if ((headsToAvoid & headMask) == 0) {
		            		addNodeAndPointer(heads[headIndex], Pointer.MIDDLE);
			            	if (findFirst(heads[headIndex], 0, 1))
			                    return true;
			            	--fillPointer;
		            	}
		            	headMask <<= 1;
		            }
	        	} else {
		            for (headIndex = 0; headIndex < heads.length; ++headIndex) {
	            		addNodeAndPointer(heads[headIndex], Pointer.MIDDLE);
		            	if (findFirst(heads[headIndex], 0, 1))
		                    return true;
		            	--fillPointer;
		            }
	        	}
	            return false;
	        } else {
	            headIndex = pattern[0] - 'A';
        		addNodeAndPointer(heads[headIndex], Pointer.MIDDLE);
            	if (findFirst(heads[headIndex], 0, 1))
                    return true;
            	--fillPointer;
    	        return false;
	        }
	    }

	    /**
	     * Find first element, starting search at specified depth.
	     * Assumes descentGraph_.size() == treeDepth + 1, where each element
	     * el is a parent node, starting with TST head at element 0
	     * Assumes childPointers_.size() == treeDepth + 1, with each element
	     * el being a pointer to the corresponding descentGraph_ node
	     * @param currNode possibly-null node we are trying to find the
	     *    first eligible pattern at
	     * @param treeDepth 0-based depth of currNode in individual TST
	     * @param keyIndex 0-based index of current char in pattern being eval'ed
	     * If found, return true and leave descentGraph_ and childPointers
	     * pointing at element. last element of descentGraph_ is the end of
	     * the next element, and last element of childPointers_ is the pointer
	     * from the parent to this last descentGraph_ element
	     */
	    private boolean findFirst(TstNode<E> currNode, int treeDepth, int keyIndex) {
	        if (currNode == null)
	            return false;
	        if (INCLUDE_AVOID) {
	        	int charsToAvoid = exclusions.length <= keyIndex ? 0 : exclusions[keyIndex];
	        }
//	        descentGraph_.add(currNode);
	        char c = pattern[keyIndex];
//	        TstNode<E> currNode = descentGraph_.get(treeDepth);
	        if (c == WILDCARD || c < currNode.splitChar) {
        		addNodeAndPointer(currNode.leftChild, Pointer.LEFT);
	            if (findFirst(currNode.leftChild, treeDepth + 1, keyIndex))
	                return true;
            	--fillPointer;
	        }
	        /*
	        if ((charsToAvoid & (1 << (currNode.splitChar_ - 'A'))) == 0 && (c == WILDCARD || c == currNode.splitChar_)) {
	        */
	        if (c == WILDCARD || c == currNode.splitChar) {
	            if (keyIndex < pattern.length - 1) {
	        		addNodeAndPointer((TstNode<E>)currNode.middleChild, Pointer.MIDDLE);
	                if (findFirst((TstNode<E>)currNode.middleChild, treeDepth + 1, keyIndex + 1))
	                    return true;
	            	--fillPointer;
	            } else if (currNode.middleChild != null) {
	                return true;
	            }
	        }
	        if (c == WILDCARD || c > currNode.splitChar) {
        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
	            if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
	                return true;
            	--fillPointer;
	        }
//	        descentGraph_.remove(descentGraph_.size() - 1);
	        return false;
	    }

	    private boolean findNext() {
	        if (pattern[0] != WILDCARD) {
	            return findNext(heads[headIndex], 0, 1);
	        }
	        
	        if (INCLUDE_AVOID) {
	        	// Skip heads we have been warned to avoid
	        	int headsToAvoid = exclusions.length == 0 ? 0 : exclusions[0];
	        	int headMask = 1 << headIndex;
	        	if ((headsToAvoid & headMask) != 0) {
	        		// Abort processing this HEAD immediately since it starts with a char we should avoid
	        		fillPointer = 0;
	        	} else {
			        if (findNext(heads[headIndex], 0, 1))
			            return true;
	        	}
		        while (++headIndex < heads.length) {
		        	headMask <<= 1;
		        	if ((headsToAvoid & headMask) == 0) {
		//System.out.println("Did not find next on existing HEAD, switching to next HEAD " + headIndex_ + " since first char wildcard");	 
		//if (descentGraph_.size() > 0 || typeOfPointerToNode_.size() > 0)
		//System.out.println("desc size " +  descentGraph_.size() + ", pointer size " + typeOfPointerToNode_);
		//	        	descentGraph_.clear();
		//	        	typeOfPointerToNode_.clear();
		        		addNodeAndPointer(heads[headIndex], Pointer.MIDDLE);
			            if (findFirst(heads[headIndex], 0, 1))
			                return true;

		            	--fillPointer;
		        	}
		        }
	        } else {
		        if (findNext(heads[headIndex], 0, 1))
		            return true;
		        while (++headIndex < heads.length) {
		//System.out.println("Did not find next on existing HEAD, switching to next HEAD " + headIndex_ + " since first char wildcard");	 
		//if (descentGraph_.size() > 0 || typeOfPointerToNode_.size() > 0)
		//System.out.println("desc size " +  descentGraph_.size() + ", pointer size " + typeOfPointerToNode_);
		//	        	descentGraph_.clear();
		//	        	typeOfPointerToNode_.clear();
	        		addNodeAndPointer(heads[headIndex], Pointer.MIDDLE);
		            if (findFirst(heads[headIndex], 0, 1))
		                return true;

	            	--fillPointer;
		        }
	        }
	        return false;
	    }

	    /**
	     * Find next element, starting search at specified depth.
	     * Upon entry, assumes descentGraph_ contains refs to sequence of elements
	     * from TST head down to last-chosen end element. And childIndexes_,
	     * 1 size smaller, contains pointers for each corresponding
	     * descentGraph_ element.
	     * Upon successful return, descentGraph_ and childIndexes_ will be
	     * set similarly, but pointing to the next end element.
	     * Upon unsuccessful return, descentGraph_ should be reduced to
	     * size treeDepth and childIndexes_ to size treeDepth - 1 
	     * @param currNode possibly-null node we are trying to find the
	     *    next eligible pattern at
	     * @param treeDepth 0-based depth of currNode in individual TST
	     * @param keyIndex 0-based index of current char in pattern being eval'ed
	     * Assumes descentGraph_[l] are set, where l in 0 .. depth
	     * Assumes childPointers_[m] are set, where m in 0 .. depth - 1
	     * If found, return true and leave descentGraph_ and childPointers
	     * pointing at element
	     * If not found, return false and pop descentGraph and childPointers
	     */
	    private boolean findNext(TstNode<E> currNode, int treeDepth, int keyIndex) {
	    	if (pattern[keyIndex] != WILDCARD) {
		    	if (treeDepth == fillPointer - 1) {
//				    if (currNode == null)
		    		--fillPointer;
		    		return false;
		    	}
	            int nextKeyindex = typeOfPointerToNode[treeDepth + 1] == Pointer.MIDDLE ? 
	                keyIndex + 1 : keyIndex;
	            if (findNext(descentGraph[treeDepth + 1], treeDepth + 1, nextKeyindex))
	                return true;
	    		--fillPointer;
	    		return false;
	    	}
	    	
	    	if (INCLUDE_AVOID) {
		    	// Processing wildcard
	        	int charsToAvoid = exclusions.length <= keyIndex ? 0 : exclusions[keyIndex];
	            boolean atEndOfKey = keyIndex == pattern.length - 1;
	            if (! atEndOfKey) {
	                Pointer prevPointerToNext = typeOfPointerToNode[treeDepth + 1];
	                
	                // Abort processing current branch if it's down a middle link whose char we seek to avoid
	                boolean splitCharBad = (charsToAvoid & (1 << (currNode.splitChar - 'A'))) != 0;
	                if (prevPointerToNext == Pointer.MIDDLE && splitCharBad) {
	                	fillPointer = treeDepth + 1;
	                } else {
	                    int nextKeyindex = typeOfPointerToNode[treeDepth + 1] == Pointer.MIDDLE ? 
	                            keyIndex + 1 : keyIndex;
	                	if (findNext(descentGraph[treeDepth + 1], treeDepth + 1, nextKeyindex))
		                    return true;
	                }
		                
	    	        // OK, try findFirst on remaining branches.
	                if (prevPointerToNext == Pointer.LEFT && ! splitCharBad) {
		        		addNodeAndPointer((TstNode<E>)currNode.middleChild, Pointer.MIDDLE);
	                	if (findFirst((TstNode<E>)currNode.middleChild, treeDepth + 1, keyIndex + 1))
	                		return true;
	                	--fillPointer;
	                }
	                if (prevPointerToNext != Pointer.RIGHT) {
		        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
	                 	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
	                		return true;
	                	--fillPointer;
	                }
		    		--fillPointer;
		    		return false;
	            }
	            
	            // OK we are processing wildcard at end of key
	            boolean atBottomOfDescent = treeDepth == fillPointer - 1;
	            if (atBottomOfDescent) {
	            	// Try right node
	        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
	             	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
	            		return true;
	             	fillPointer -= 2;
	             	return false;
	            } else {
	            	// Processing wildcard, not at bottom of descent
		            Pointer prevPointerToNext = typeOfPointerToNode[treeDepth + 1];
	                // Abort processing current branch if it's down a middle link whose char we seek to avoid
	                boolean splitCharBad = (charsToAvoid & (1 << (currNode.splitChar - 'A'))) != 0;
	                if (prevPointerToNext == Pointer.MIDDLE && splitCharBad) {
	                	fillPointer = treeDepth + 1;
	                } else {
	    	            int nextKeyindex = typeOfPointerToNode[treeDepth + 1] == Pointer.MIDDLE ? 
	    		                keyIndex + 1 : keyIndex;
	                	if (findNext(descentGraph[treeDepth + 1], treeDepth + 1, nextKeyindex))
		                    return true;
	                }
	
	                // If we're at end of pattern, and current node is itself an entry, and we were previously down left branch, return it
		            if (currNode.middleChild != null && prevPointerToNext == Pointer.LEFT)
		            	return true;
		            
			        // OK, try now findFirst on right branch, if we haven't already
		            if (prevPointerToNext != Pointer.RIGHT) {
		        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
		             	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
		            		return true;
		            	--fillPointer;
		            }
	            	--fillPointer;
	            	return false;
	            }
	    	} else {
		    	// Processing wildcard
	            boolean atEndOfKey = keyIndex == pattern.length - 1;
	            if (! atEndOfKey) {
	                Pointer prevPointerToNext = typeOfPointerToNode[treeDepth + 1];
	                
                    int nextKeyindex = typeOfPointerToNode[treeDepth + 1] == Pointer.MIDDLE ? 
                            keyIndex + 1 : keyIndex;
                	if (findNext(descentGraph[treeDepth + 1], treeDepth + 1, nextKeyindex))
	                    return true;
		                
	    	        // OK, try findFirst on remaining branches.
	                if (prevPointerToNext == Pointer.LEFT) {
		        		addNodeAndPointer((TstNode<E>)currNode.middleChild, Pointer.MIDDLE);
	                	if (findFirst((TstNode<E>)currNode.middleChild, treeDepth + 1, keyIndex + 1))
	                		return true;
	                	--fillPointer;
	                }
	                if (prevPointerToNext != Pointer.RIGHT) {
		        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
	                 	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
	                		return true;
	                	--fillPointer;
	                }
		    		--fillPointer;
		    		return false;
	            }
	            
	            // OK we are processing wildcard at end of key
	            boolean atBottomOfDescent = treeDepth == fillPointer - 1;
	            if (atBottomOfDescent) {
	            	// Try right node
	        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
	             	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
	            		return true;
	             	fillPointer -= 2;
	             	return false;
	            } else {
	            	// Processing wildcard, not at bottom of descent
		            Pointer prevPointerToNext = typeOfPointerToNode[treeDepth + 1];
    	            int nextKeyindex = typeOfPointerToNode[treeDepth + 1] == Pointer.MIDDLE ? 
    		                keyIndex + 1 : keyIndex;
                	if (findNext(descentGraph[treeDepth + 1], treeDepth + 1, nextKeyindex))
	                    return true;
	
	                // If we're at end of pattern, and current node is itself an entry, and we were previously down left branch, return it
		            if (currNode.middleChild != null && prevPointerToNext == Pointer.LEFT)
		            	return true;
		            
			        // OK, try now findFirst on right branch, if we haven't already
		            if (prevPointerToNext != Pointer.RIGHT) {
		        		addNodeAndPointer(currNode.rightChild, Pointer.RIGHT);
		             	if (findFirst(currNode.rightChild, treeDepth + 1, keyIndex))
		            		return true;
		            	--fillPointer;
		            }
	            	--fillPointer;
	            	return false;
	            }
	    	}
	    }
	}
}
