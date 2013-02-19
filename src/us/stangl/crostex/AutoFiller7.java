/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.util.CircularList;
import us.stangl.crostex.util.Stack;

/**
 * 7th generation implementation of auto-fill algorithm.
 * Uses dynamic backtrack, interative broadening, cheapest-first heuristic.
 */
public class AutoFiller7 implements AutoFill {
	public boolean autoFill(Grid grid, Dictionary<char[], Word> dict) {
		// Find all full words in xword, store in set for duplicate checking
		Set<String> wordsAlreadyInUse = new HashSet<String>();
		List<GridWord> acrossWordsToFill = new ArrayList<GridWord>();
		List<GridWord> downWordsToFill = new ArrayList<GridWord>();

		for (GridWord gw : grid.getAcrossWords()) {
			if (gw.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(gw.getPattern()))
					return false;						// cannot satisfy this across word
				acrossWordsToFill.add(gw);
			} else if (gw.isComplete()) {
				wordsAlreadyInUse.add(gw.getContents());
			}
		}

		for (GridWord gw : grid.getDownWords()) {
			if (gw.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(gw.getPattern()))
					return false;						// cannot satisfy this down word
				downWordsToFill.add(gw);
			} else if (gw.isComplete()) {
				wordsAlreadyInUse.add(gw.getContents());
			}
		}

		// Generate array of references from each partial across word to
		// partial down words they intersect with a blank
		Map<GridWord, List<GridWord>> wordToCrossingWordsMap = new HashMap<GridWord, List<GridWord>>();
		for (GridWord agw : acrossWordsToFill) {
			List<GridWord> list = new ArrayList<GridWord>();
			for (GridWord dgw : downWordsToFill) {
				Cell intersection = agw.getIntersection(dgw);
				if (intersection != null && intersection.isEligibleForAutofill()) {
					list.add(dgw);
				}
			}
			wordToCrossingWordsMap.put(agw, list);
		}

		// Generate array of references from each partial down word to partial across words they intersect with a blank
		for (GridWord dgw : downWordsToFill) {
			List<GridWord> list = new ArrayList<GridWord>();
			for (GridWord agw : acrossWordsToFill) {
				Cell intersection = dgw.getIntersection(agw);
				if (intersection != null && intersection.isEligibleForAutofill()) {
					list.add(agw);
				}
			}
			wordToCrossingWordsMap.put(dgw, list);
		}

		// free up things we don't really need
		wordsAlreadyInUse = null;

		int maxBreadth = 3;

		// Build a Map from every GridWord -> its list of candidates, and keep it up to date, for arc consistency
		Map<GridWord, List<Pair<char[], Word>>> wordPossibilities = new HashMap<GridWord, List<Pair<char[], Word>>>();
		Map<GridWord, WorkTuple> wordToTupleMap = new HashMap<GridWord, WorkTuple>();
		for (GridWord word : wordToCrossingWordsMap.keySet()) {
			wordPossibilities.put(word, dict.getPatternMatches(word.getPattern()));
			// Create new work tuple for this word, and put it into map
			WorkTuple workTuple = new WorkTuple(word, wordPossibilities, dict, wordToCrossingWordsMap, maxBreadth, wordToTupleMap);
			wordToTupleMap.put(word, workTuple);
		}

		Stack<WorkTuple> workStack = new Stack<WorkTuple>();

		int maxNumAttempts = 50000;
		int attemptNumber = 0;
//BREADTHLOOP:
//		for (maxBreadth = 2; ; ++maxBreadth) {
//			for (WorkTuple tuple : wordToTupleMap.values())
//				tuple.breadth_ = maxBreadth;
//System.out.println("Setting breadth to " + maxBreadth);

MAINLOOP:
		while (true) {
			if (++attemptNumber > maxNumAttempts) {
System.out.println("Returning false because have reached " + attemptNumber + " attempts.");
				return false;
			}
			
//			grid.printToStream(System.out);
			
			// Now find "cheapest" candidate to work next, the one with the fewest number of possibilities
			int lowestNumChoicesSoFar = Integer.MAX_VALUE;
			GridWord cheapestWord = null;
			for (Map.Entry<GridWord, List<Pair<char[], Word>>> entry : wordPossibilities.entrySet()) {
				GridWord word = entry.getKey();
//System.out.println("Considering " + word);
				if (word.isComplete()) {
//System.out.println("... but it's complete");
					continue;
				}
//				int size = entry.getValue().size();
				// Pull tuple from map and see how many possibilities it has
				WorkTuple workTuple = wordToTupleMap.get(word);
				int size = workTuple.choices_.size();
//				if (size == 0) {
//					// We would normally need to backtrack here, but since it's our initial foray, we conclude grid cannot be filled
////TODO fix this scenario
//System.out.println("Returning false because word has size 0: " + word + ", has " + workTuple.choices_.size() + " choices and " + workTuple.explanations_.size() + " explanations.");
//					return false;
//				}

				if (size < lowestNumChoicesSoFar) {
					lowestNumChoicesSoFar = size;
					cheapestWord = word;
				}
			}

			if (cheapestWord == null)
				return true;					// Done!

//System.out.println("cheapestWord " + lowestNumChoicesSoFar + " choices: " + cheapestWord);
			// Get corresponding tuple from map and see if we can get a good choice for it
			WorkTuple workTuple = wordToTupleMap.get(cheapestWord);
			if (workTuple.getBestChoiceOrRollback()) {
//System.out.println("Pushing tuple for " + cheapestWord);
				workStack.push(workTuple);
				
				// Add all newly completed cross words
				for (GridWord crossWord : wordToCrossingWordsMap.get(cheapestWord))
					if (crossWord.isComplete()) {
						WorkTuple crossTuple = wordToTupleMap.get(crossWord);
						if (! workStack.contains(crossTuple))
							workStack.push(crossTuple);
					}
				
			} else {
				// Backtrack here: Work back up stack looking for a word that we can we change, using secondary stack
				// to hold items pulled off the primary stack when rolling back, then push 'em all back on to the primary

//System.out.println("Couldn't get nextBestChoice for " + cheapestWord + ", adding to wordThatNeedChange and backtracking");
				// Call the elimination mechanism here (latter part of step 2) to add any eliminations, assuming we really handle it that way
				// for a (partial solution, workTuple), return a set of eliminating explanations (char[], Set<GridWord>)
				// In this case, for a leaf node, we will simply use the set of all previous intersecting words

				Set<GridWord> eliminatingExplanationParents = new HashSet<GridWord>();
				for (GridWord crossingWord : wordToCrossingWordsMap.get(cheapestWord))
					if (crossingWord.isComplete())
						eliminatingExplanationParents.add(crossingWord);

//System.out.println("eliminatingExplanationParents for  " + cheapestWord);
//for (GridWord gw : eliminatingExplanationParents)
//	System.out.println(gw);

				// use these eliminating explanation parents in the rollback now
				// Get set of candidates of gridwords responsible for our current failure. These are candidates to backtrack to
				Set<GridWord> backtrackCandidates = workTuple.getExplainingGridwords();
				eliminatingExplanationParents.addAll(backtrackCandidates);
//System.out.println("eliminatingExplanationParents for  " + workTuple.word_);
//for (GridWord gw : eliminatingExplanationParents)
//	System.out.println(gw);
//				backtrackCandidates.addAll(eliminatingExplanationParents);
				if (eliminatingExplanationParents.isEmpty()) {
System.out.println("Returning false because " + cheapestWord + " has no backtrack candidates. It has " + workTuple.choices_.size() + " choices and " + workTuple.explanations_.size() + " explanations.");
System.out.println("workTuple.getPattern = " + new String(workTuple.getPattern()));
System.out.println("eliminatingExplanationParents is of size " + eliminatingExplanationParents.size() + " and backtrackCandidates is of size " + backtrackCandidates.size());
//		private Map<char[], Pair<Pair<char[], Word>, Set<GridWord>>> explanations_ = new HashMap<char[], Pair<Pair<char[], Word>, Set<GridWord>>>();

for (Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>> entry : workTuple.explanations_.entrySet()) {
	System.out.println("Entry key = " + new String(entry.getKey()) + ", eliminated value = " + new String(entry.getValue().first.first));
	for (GridWord gw : entry.getValue().second) {
		System.out.println("gw = " + gw);
	}
}
					return false;								// No candidates -- we have failed
				}

				Stack<WorkTuple> tempStack = new Stack<WorkTuple>();
				while (! workStack.empty()) {
					workTuple = workStack.pop();
					GridWord currWord = workTuple.word_;
					// Backtrack until find another workTuple whose word is bound to an explanation of the one we are backtracking from
					if (! eliminatingExplanationParents.contains(currWord)) {
						tempStack.push(workTuple);
					} else if (!workTuple.canBackTrack()) {
//System.out.println("Skipping over " + currWord);
						workTuple.breadth_ = maxBreadth;
						for (WorkTuple otherTuple : wordToTupleMap.values())
							otherTuple.removeFromExplanations(currWord);

						// Propagate eliminating explanation back to earlier node
						//TODO what if backtrackCandidates ONLY contains currWord???? Something wrong here!!!
						eliminatingExplanationParents.remove(currWord);
//System.out.println("adding candidates to current workTuple explanations: in " + workTuple.word_);
//for (GridWord gw : eliminatingExplanationParents)
//	System.out.println(gw);
//System.out.println("Calling addExplanationAndUndo from first site with " + eliminatingExplanationParents.size());						
						workTuple.addExplanationAndUndo(eliminatingExplanationParents);
						// merge prev. backtrack explanations with this new backtrack explanations
						eliminatingExplanationParents = workTuple.getExplainingGridwords();
						for (GridWord crossingWord : wordToCrossingWordsMap.get(currWord))
							if (crossingWord.isComplete())
								eliminatingExplanationParents.add(crossingWord);
//						tempStack.push(workTuple);
					} else {
//System.out.println("currWord" + currWord + " is in backTrackCandidates");
						// push tempStack contents back, leaving curr node at bottom
						while (! tempStack.empty())
							workStack.push(tempStack.pop());

						for (WorkTuple otherTuple : wordToTupleMap.values())
							otherTuple.removeFromExplanations(currWord);

						// Propagate eliminating explanation back to earlier node
						//TODO what if backtrackCandidates ONLY contains currWord???? Something wrong here!!!
						eliminatingExplanationParents.remove(currWord);
//System.out.println("adding candidates to current workTuple explanations: in " + workTuple.word_);
//for (GridWord gw : eliminatingExplanationParents)
//	System.out.println(gw);
//System.out.println("Calling addExplanationAndUndo from 2nd site with " + eliminatingExplanationParents.size());						
						workTuple.addExplanationAndUndo(eliminatingExplanationParents);
						continue MAINLOOP;
					}

					if (workStack.empty()) {
						// Reconstruct stack (by swapping) and increase breadth
						Stack<WorkTuple> tempStackPtr = tempStack;
						tempStack = workStack;
						workStack = tempStackPtr;
						++maxBreadth;
System.out.println("Setting breadth to " + maxBreadth);
						for (WorkTuple tuple : wordToTupleMap.values())
							tuple.breadth_ = maxBreadth;
					}
				}
//				continue BREADTHLOOP;
//System.out.println("Bailing because I couldn't find anything matching: ");
//for (GridWord cand : backtrackCandidates)
//	System.out.println(cand);
//				return false;				// We backtracked all the way out and couldn't find a solution
			}
//		}
		}
	}

	private static class WorkTuple {
		public final GridWord word_;

		public final Pair<Cell[], int[]> savedConfig_;

		public List<GridWord> children_;

		private CircularList<Pair<char[], Word>> choices_ = new CircularList<Pair<char[], Word>>();

		/** last choice */
		private Pair<char[], Word> lastChoice_;
		
		/** index into each member of children pattern, of intersection with word_ */
		private int[] childIndex_;

		/** index into word_ pattern, of intersection with each corresponding member of children_ */
		private int[] parentIndex_;

		/** pattern for this word */
		char[] currPattern_;

		/** children patterns */
//		private char[][] childPatterns_;

		/** word-to-word crossings map */
		private final Map<GridWord, List<GridWord>> wordToCrossingWordsMap_;

		/** breadth -- max # of backtracks to allow before forcing further backtrack */
		private int breadth_;

		/** dictionary */
		private final Dictionary<char[], Word> dict_;
		
		/** GridWord -> tuple map */
		private final Map<GridWord, WorkTuple> wordToTupleMap_;
		
		/** individual explanations */
		private Map<char[], Pair<Pair<char[], Word>, Set<GridWord>>> explanations_ = new HashMap<char[], Pair<Pair<char[], Word>, Set<GridWord>>>();
		
		/** Cell -> crossing word map */
		private Map<Cell, GridWord> cellToCrossingWordMap_ = new HashMap<Cell, GridWord>();

		public WorkTuple(GridWord word, Map<GridWord, List<Pair<char[], Word>>> wordPossibilities, Dictionary<char[], Word> dict,
				Map<GridWord, List<GridWord>> wordToCrossingWordsMap, int breadth, Map<GridWord, WorkTuple> wordToTupleMap)
		{
			word_ = word;
			savedConfig_ = word.getFillConfig();
			choices_.addAll(wordPossibilities.get(word));
			wordToCrossingWordsMap_ = wordToCrossingWordsMap;
			breadth_ = breadth;
			dict_ = dict;
			wordToTupleMap_ = wordToTupleMap;
			
			// get list of only the children that need filling
			List<GridWord> allChildren = wordToCrossingWordsMap.get(word_);
			children_ = new ArrayList<GridWord>(allChildren.size());
			for (GridWord child : allChildren)
				if (! child.isComplete())
					children_.add(child);

			currPattern_ = copyCharArray(word_.getPattern());
			// for each child, store index in child of intersection with our word, and index in our word
			// This makes the intelligent instantiation much faster, by having child patterns and index into places
			// to change in the patterns all ready to go.
			childIndex_ = new int[children_.size()];
			parentIndex_ = new int[children_.size()];
//			childPatterns_ = new char[children_.size()][];
			for (int i = 0; i < children_.size(); ++i) {
				GridWord child = children_.get(i);
				Cell intersection = child.getIntersection(word_);
				childIndex_[i] = child.indexOfCellInPattern(intersection);
				parentIndex_[i] = word_.indexOfCellInPattern(intersection);
//				childPatterns_[i] = copyCharArray(child.getPattern());
			}

			// build map of Cell -> crossing word
			for (GridWord crossingWord : wordToCrossingWordsMap_.get(word_))
				cellToCrossingWordMap_.put(word_.getIntersection(crossingWord), crossingWord);
//System.out.println("leaving ctor, currPattern_ = " + new String(currPattern_));
		}

		/** Set (copies) pattern and its matches to the specified values */
		public void setPattern(char[] pattern, List<Pair<char[], Word>> matches) {
//System.out.println("in setPattern(" + new String(pattern) + " vs currPattern_ " + new String(currPattern_));
			if (copyAndCompare(pattern, currPattern_))
				return;
			choices_ = new CircularList<Pair<char[], Word>>();

			Set<char[]> explanationKeys = explanations_.keySet();
			for (Pair<char[], Word> match : matches)
				if (! explanationKeys.contains(match.first))
					choices_.add(match);
//System.out.println("Exiting setPattern, currPattern_ = " + new String(currPattern_));			
		}

		/** copy source to dest and return true if they were already equal */
		private boolean copyAndCompare(char[] src, char[] dest) {
			boolean retval = true;
			for (int i = 0; i < src.length; ++i)
				if (dest[i] != src[i]) {
					dest[i] = src[i];
					retval = false;
				}
			return retval;
		}

		public boolean canBackTrack() {
			return breadth_-- > 0;
		}

		public void resetPattern() {
			choices_ = new CircularList<Pair<char[], Word>>();
//System.out.println("in resetPattern, was " + new String(currPattern_) + ", now " + new String(getPattern()));			
			currPattern_ = getPattern();

			// exclude anything in an explanation
			Set<char[]> explanationKeys = explanations_.keySet();
			for (Pair<char[], Word> match : dict_.getPatternMatches(currPattern_))
				if (! explanationKeys.contains(match.first))
					choices_.add(match);
//System.out.println("leaving resetPattern, currPattern_ = " + new String(currPattern_));			
		}

		// Include not only intersecting words in explanation for failure.
		// If we reject words because intersecting words have no choices,
		// then include all the full words each of those 0-choice
		// intersecting words intersects with too
		public boolean getBestChoiceOrRollback() {
			int choicesChecked = 0;
			int choicesSize = choices_.size();
			char[][] childPatterns_ = new char[children_.size()][];

			// Update childPatterns. NOTE we copy so we don't mutate child's pattern
			//TODO If child is a fully-formed word, we don't want to destroy it here!
			//TODO figure out which children intersection cells can be written and which must not, and thereby figure out our pattern here
			for (int i = 0; i < children_.size(); ++i) {
//				char[] childPat = wordToTupleMap_.get(children_.get(i)).currPattern_;
				char[] childPat = wordToTupleMap_.get(children_.get(i)).getPattern();
				childPatterns_[i] = childPat;
//System.out.println("Child " + i + " currPattern_ = " + new String(childPat) + " vs getPattern() = " + new String(patCopy) + " vs GridWord.getPattern " + new String(children_.get(i).getPattern()));
//childPat = patCopy;
//				for (int j = 0; j < childPat.length; ++j)
//					childPatterns_[i][j] = childPat[j];
			}
			
			List<Pair<char[], Word>>[] childMatches = new List[childPatterns_.length];
			// get next choice
			Set<char[]> explanationKeys = explanations_.keySet();
CHOICELOOP:	while (choicesChecked++ < choicesSize) {
				Pair<char[], Word> choice = choices_.getNext();
				char[] choiceWord = choice.first;
				// Check and see if this choiceWord has been eliminated
				if (explanationKeys.contains(choiceWord))
					continue;

				// Now check all its children
				for (int i = 0; i < childPatterns_.length; ++i) {
					char[] pattern = childPatterns_[i];
					pattern[childIndex_[i]] = choiceWord[parentIndex_[i]];
					childMatches[i] = dict_.getPatternMatches(pattern);
					if (childMatches[i].size() == 0) {
						addChildExplanation(choice, children_.get(i));
						continue CHOICELOOP;
					}
				}
//System.out.println("Setting contents " + new String(choiceWord));
				// Put best word in and update candidates for all its intersecting words, then loop back for next cheapest word choice
				word_.setAutofillContents(choiceWord, savedConfig_);
				
				// Update all children's patterns and matches
				for (int i = 0; i < children_.size(); ++i) {
					GridWord child = children_.get(i);
					WorkTuple childTuple = wordToTupleMap_.get(child);
//System.out.println("Child has " + new String(childTuple.word_.getPattern()) + ", overriding with pattern " + new String(childPatterns_[i]));					
					childTuple.setPattern(childPatterns_[i], childMatches[i]);
					
					if (childTuple.choices_.size() == 0) {
//System.out.println("ChildTuple.choices_.size() == 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");						
						//TODO need to rollback prev. work here!!!
						addChildExplanation(choice, child);
						continue CHOICELOOP;
					}
				}
				lastChoice_ = choice;
//System.out.println("returning true");				
				return true;
			}

			// backtrack: undo this word, restore arc consistency of neighbors
//System.out.println("undo, return false");
			undoWord();
			return false;
		}

		private void addChildExplanation(Pair<char[], Word> choice, GridWord child) {
			char[] choiceWord = choice.first;
			// add explanation with child's cross words, and delete choice
			Set<GridWord> eliminatingExplanation = new HashSet<GridWord>();
			for (GridWord crossingWord : wordToCrossingWordsMap_.get(child))
				if (crossingWord != word_ && crossingWord.isComplete())
					eliminatingExplanation.add(crossingWord);
			explanations_.put(choiceWord,
					new Pair<Pair<char[], Word>, Set<GridWord>>(choice, eliminatingExplanation));
			choices_.deletePrev();
		}
		
		/** Remove all explanations associated with specified gridWord. */
		private void removeFromExplanations(GridWord gridWord) {
			char[] currPattern = getPattern();
			for (Iterator<Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>>> it = explanations_.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>> entry = it.next();
				Pair<Pair<char[], Word>, Set<GridWord>> value = entry.getValue();
				if (value.second.contains(gridWord)) {
					it.remove();
					// Only add back to choices if it conforms to current pattern
					if (conformsToPattern(value.first.first, currPattern))
						choices_.add(value.first);
				}
			}
		}
		
		/** return whether specified value matches pattern_, expected to be of same length */
		private boolean conformsToPattern(char[] value) {
			return conformsToPattern(value, currPattern_);
		}

		/** return whether specified value matches specified pattern, expected to be of same length */
		private boolean conformsToPattern(char[] value, char[] pattern) {
//			if (value.length != currPattern_.length)
//				return false;
			for (int i = 0; i < pattern.length; ++i)
				if (pattern[i] != '_' && pattern[i] != value[i])
					return false;
			return true;
		}

		public void addExplanationAndUndo(Set<GridWord> gridWords) {
			// Not checking for existing explanation here because there shouldn't be one yet
			Pair<Pair<char[], Word>, Set<GridWord>> explanation = new Pair<Pair<char[], Word>, Set<GridWord>>
				(lastChoice_, new HashSet<GridWord>(gridWords));
			explanations_.put(lastChoice_.first, explanation);

			// Remove the most recent choice from choices, and undo
			choices_.deletePrev();
			undoWord();
			
			// reset pattern and choices, if necessary
			resetPattern();
		}

		/**
		 * @return explainingGridwords, all GridWord involved in explanations
		 */
		public Set<GridWord> getExplainingGridwords() {
			Set<GridWord> retval = new HashSet<GridWord>();
			for (Pair<Pair<char[], Word>, Set<GridWord>> value : explanations_.values())
				retval.addAll(value.second);
			return retval;
		}

		/**
		 * return pattern for word, taking into account which intersecting words
		 * are completed. Assumes pattern length will not change. Returns new array
		 */
		private char[] getPattern() {
			char[] retval = new char[currPattern_.length];
			int index = 0;
			for (Cell cell : word_.getCells()) {
				if (cell.isEligibleForAutofill() || ! cellToCrossingWordMap_.get(cell).isComplete()) {
					retval[index++] = '_';
				} else {
					cell.appendContents(retval, index);
					index += cell.getContentsSize();
				}
			}
			return retval;
		}

		/** clear chars from word, except those involved in complete cross words, reset affected child's patterns */
		private void undoWord() {
			for (Cell cell : word_.getCells()) {
				GridWord crossWord = cellToCrossingWordMap_.get(cell);
				if (! crossWord.isComplete()) {
					// Clear cell, reset child pattern and matches
					cell.setEmpty();
					wordToTupleMap_.get(crossWord).resetPattern();
				}
			}
//			currPattern_ = getPattern();
		}

		/** return a copy of the specified char[] */
		private char[] copyCharArray(char[] input) {
			char[] retval = new char[input.length];
			for (int i = 0; i < input.length; ++i)
				retval[i] = input[i];
			return retval;
		}
	}
}
