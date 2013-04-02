/**
 * Copyright 2008-2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.autofill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.GridWord;
import us.stangl.crostex.Word;
import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.util.CircularList;
import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.RowColumnPair;
import us.stangl.crostex.util.Stack;

/**
 * 7th generation implementation of auto-fill algorithm.
 * Uses dynamic backtrack, interative broadening, cheapest-first heuristic.
 * @author Alex Stangl
 */
public class AutoFiller7 implements AutoFillGrid, AutoFillRegion {
	@Override
	public boolean autoFillRegion(Grid grid, Dictionary<char[], Word> dict) {
		int row = grid.getCurrentRow();
		int column = grid.getCurrentColumn();
		Cell cell = grid.getCell(row, column);
		if (! cell.isEligibleForAutofill())
			return true;			// TODO -- reconsider whether this is the best way to handle this scenario
	
		Set<RowColumnPair> region = new HashSet<RowColumnPair>();
		findRegion(row, column, grid, region);
		List<GridWord> acrossWords = intersectWordsWithRegion(grid.getAcrossWords(), region);
		List<GridWord> downWords = intersectWordsWithRegion(grid.getDownWords(), region);
		return autoFillImpl(grid, dict, acrossWords, downWords);
	}

	// performing a flood-fill-like algorithm, find transitive closure of all empty cells connected to specified cell
	private void findRegion(int row, int column, Grid grid, Set<RowColumnPair> region) {
		if (row >= 0 && column >= 0 && row < grid.getHeight() && column < grid.getWidth()) {
			RowColumnPair pair = new RowColumnPair(row, column);
			if (! region.contains(pair)) {
				region.add(pair);
				findRegion(row - 1, column, grid, region);
				findRegion(row + 1, column, grid, region);
				findRegion(row, column - 1, grid, region);
				findRegion(row, column + 1, grid, region);
			}
		}
	}
	
	// filter specified GridWords, returning list of only those intersecting with specified region
	private List<GridWord> intersectWordsWithRegion(Collection<GridWord> gridWords, Set<RowColumnPair> region) {
		List<GridWord> retval = new ArrayList<GridWord>();
		for (GridWord gridWord : gridWords)
			if (intersectsRegion(gridWord, region))
				retval.add(gridWord);
		return retval;
	}

	// return whether specified GridWord intersects specified region
	private boolean intersectsRegion(GridWord gridWord, Set<RowColumnPair> region) {
		for (RowColumnPair pair : gridWord.getCoordinatesOfAllCells())
			if (region.contains(pair))
				return true;
		return false;
	}

	
	@Override
	public boolean autoFill(Grid grid, Dictionary<char[], Word> dict) {
		return autoFillImpl(grid, dict, grid.getAcrossWords(), grid.getDownWords());
	}
	
	private boolean autoFillImpl(Grid grid, Dictionary<char[], Word> dict, List<GridWord> acrossWords, List<GridWord> downWords) {
		// Find all full words in xword, store in set for duplicate checking
		Set<String> wordsAlreadyInUse = new HashSet<String>();
		List<GridWord> acrossWordsToFill = getWordsToFill(acrossWords, wordsAlreadyInUse, dict);
		List<GridWord> downWordsToFill = getWordsToFill(downWords, wordsAlreadyInUse, dict);

		if (acrossWordsToFill == null || downWordsToFill == null)
			return false;			// at least one word pattern could not be satisfied by auto-fill

		// Generate map of references from each partial across word to partial down words they intersect with a blank
		Map<GridWord, List<GridWord>> wordToCrossingWordsMap = findIntersections(acrossWordsToFill, downWordsToFill);

		// free up things we don't really need
		wordsAlreadyInUse = null;

		int maxBreadth = 3;

		// Build a Map from every GridWord -> its list of candidates, and keep it up to date, for arc consistency
		Map<GridWord, List<Pair<char[], Word>>> wordPossibilities = new HashMap<GridWord, List<Pair<char[], Word>>>();
		Map<GridWord, WorkTuple> wordToTupleMap = new HashMap<GridWord, WorkTuple>();
		for (GridWord word : wordToCrossingWordsMap.keySet()) {
			List<Pair<char[], Word>> matches = dict.getPatternMatches(word.getPattern());
			wordPossibilities.put(word, matches);
			// Create new work tuple for this word, and put it into map
			wordToTupleMap.put(word, new WorkTuple(word, matches, dict, wordToCrossingWordsMap, maxBreadth, wordToTupleMap));
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
			
			// Now find "cheapest" candidate to work next: the one with the fewest # of possibilities
			GridWord cheapestWord = findCheapestCandidate(wordPossibilities, wordToTupleMap);
			if (cheapestWord == null)
				return true;					// Done!

			// Get associated tuple from map, see if we can get a good choice for it
			WorkTuple workTuple = wordToTupleMap.get(cheapestWord);
			if (workTuple.getBestChoiceOrRollback()) {
				workStack.push(workTuple);
				
				// Add all newly completed cross words to workStack
				for (GridWord crossWord : wordToCrossingWordsMap.get(cheapestWord))
					if (crossWord.isComplete()) {
						WorkTuple crossTuple = wordToTupleMap.get(crossWord);
						if (! workStack.contains(crossTuple))
							workStack.push(crossTuple);
					}
			} else {
				// Backtrack here: Work back up stack looking for a word that we can
				// change, using secondary stack to hold items pulled off the primary
				// stack when rolling back, then push 'em all back on to the primary

//System.out.println("Couldn't get nextBestChoice for " + cheapestWord + ", adding to wordThatNeedChange and backtracking");
				// Call the elimination mechanism here (latter part of step 2) to add
				// any eliminations, assuming we really handle it that way
				// for a (partial solution, workTuple), return a set of eliminating explanations (char[], Set<GridWord>)
				// In this case, for a leaf node, we will simply use the set of all previous intersecting words

				Set<GridWord> eliminatingExplanationParents = new HashSet<GridWord>();
				for (GridWord crossingWord : wordToCrossingWordsMap.get(cheapestWord))
					if (crossingWord.isComplete())
						eliminatingExplanationParents.add(crossingWord);

				// use these eliminating explanation parents in the rollback now
				// Get set of candidates of gridwords responsible for our current failure. These are candidates to backtrack to
				Set<GridWord> backtrackCandidates = workTuple.getExplainingGridwords();
				eliminatingExplanationParents.addAll(backtrackCandidates);
//				backtrackCandidates.addAll(eliminatingExplanationParents);
				if (eliminatingExplanationParents.isEmpty()) {
					System.out.println("Returning false because " + cheapestWord + " has no backtrack candidates. It has " + workTuple.choices.size() + " choices and " + workTuple.explanations.size() + " explanations.");
					System.out.println("workTuple.computePattern = " + new String(workTuple.computePattern()));
					System.out.println("eliminatingExplanationParents is of size " + eliminatingExplanationParents.size() + " and backtrackCandidates is of size " + backtrackCandidates.size());

					for (Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>> entry : workTuple.explanations.entrySet()) {
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
					GridWord currWord = workTuple.word;
					// Backtrack until find another workTuple whose word is bound to an explanation of the one we are backtracking from
					if (! eliminatingExplanationParents.contains(currWord)) {
						tempStack.push(workTuple);
					} else if (!workTuple.canBackTrack()) {
//System.out.println("Skipping over " + currWord);
						workTuple.breadth = maxBreadth;
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
							tuple.breadth = maxBreadth;
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

	// from specified words, populate wordsAlreadyInUse w/ completed words and return words to fill,
	// or null if unable to auto-fill
	private List<GridWord> getWordsToFill(Collection<GridWord> words, Set<String> wordsAlreadyInUse, Dictionary<char[], Word> dict) {
		List<GridWord> retval = new ArrayList<GridWord>();

		for (GridWord word : words) {
			if (word.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(word.getPattern()))
					return null;						// cannot satisfy this word
				retval.add(word);
			} else if (word.isComplete()) {
				wordsAlreadyInUse.add(word.getContents());
			}
		}
		return retval;
	}

	// return map of all blank intersections from across words to down words, and vice versa
	private Map<GridWord, List<GridWord>> findIntersections(Collection<GridWord> acrossWords,
			Collection<GridWord> downWords)
	{
		Map<GridWord, List<GridWord>> retval = new HashMap<GridWord, List<GridWord>>();
		findIntersections(acrossWords, downWords, retval);
		findIntersections(downWords, acrossWords, retval);
		return retval;
	}

	// populate map of words to perpendicular words intersecting with a blank
	private void findIntersections(Collection<GridWord> fromWords, Collection<GridWord> toWords,
			Map<GridWord, List<GridWord>> intersections)
	{
		for (GridWord fromWord : fromWords) {
			List<GridWord> list = new ArrayList<GridWord>();
			for (GridWord toWord : toWords) {
				Cell intersection = fromWord.getIntersection(toWord);
				if (intersection != null && intersection.isEligibleForAutofill())
					list.add(toWord);
			}
			intersections.put(fromWord, list);
		}
	}


	// return "cheapest" candidate to work next, the one with the fewest number of possibilities
	private GridWord findCheapestCandidate(Map<GridWord, List<Pair<char[], Word>>> wordPossibilities, Map<GridWord, WorkTuple> wordToTupleMap) {
		GridWord retval = null;
		int lowestNumChoicesSoFar = Integer.MAX_VALUE;
		for (Map.Entry<GridWord, List<Pair<char[], Word>>> entry : wordPossibilities.entrySet()) {
			GridWord word = entry.getKey();
			if (word.isComplete())
				continue;
			// Pull tuple from map and see how many possibilities it has
			int size = wordToTupleMap.get(word).choices.size();
			//				if (size == 0) {
			//					// We would normally need to backtrack here, but since it's our initial foray, we conclude grid cannot be filled
			////TODO fix this scenario
			//System.out.println("Returning false because word has size 0: " + word + ", has " + workTuple.choices_.size() + " choices and " + workTuple.explanations_.size() + " explanations.");
			//					return false;
			//				}

			if (size < lowestNumChoicesSoFar) {
				lowestNumChoicesSoFar = size;
				retval = word;
			}
		}
		return retval;
	}


	private static class WorkTuple {
		public final GridWord word;

		public final Pair<Cell[], int[]> savedConfig;

		public List<GridWord> children;

		private CircularList<Pair<char[], Word>> choices = new CircularList<Pair<char[], Word>>();

		private Pair<char[], Word> lastChoice;
		
		// index into each member of children pattern, of intersection with word
		private int[] childIndex;

		// index into word pattern, of intersection with each corr. member of children
		private int[] parentIndex;

		// pattern for this word
		private char[] currPattern;

		/** word-to-word crossings map */
		private final Map<GridWord, List<GridWord>> wordToCrossingWordsMap;

		// breadth -- max # of backtracks to allow before forcing further backtrack
		private int breadth;

		private final Dictionary<char[], Word> dict;
		
		// GridWord -> tuple map
		private final Map<GridWord, WorkTuple> wordToTupleMap;
		
		// individual explanations
		private Map<char[], Pair<Pair<char[], Word>, Set<GridWord>>> explanations = new HashMap<char[], Pair<Pair<char[], Word>, Set<GridWord>>>();
		
		// Cell -> crossing word map
		private Map<Cell, GridWord> cellToCrossingWordMap = new HashMap<Cell, GridWord>();

		public WorkTuple(GridWord word, List<Pair<char[], Word>> matches, Dictionary<char[], Word> dict,
				Map<GridWord, List<GridWord>> wordToCrossingWordsMap, int breadth, Map<GridWord, WorkTuple> wordToTupleMap)
		{
			this.word = word;
			this.savedConfig = word.getFillConfig();
			this.choices.addAll(matches);
			this.wordToCrossingWordsMap = wordToCrossingWordsMap;
			this.breadth = breadth;
			this.dict = dict;
			this.wordToTupleMap = wordToTupleMap;
			
			// get list of only the children that need filling
			List<GridWord> allChildren = wordToCrossingWordsMap.get(this.word);
			this.children = new ArrayList<GridWord>(allChildren.size());
			for (GridWord child : allChildren)
				if (! child.isComplete())
					children.add(child);

			this.currPattern = copyCharArray(this.word.getPattern());
			// for each child, store index in child of intersection with our word, and index in our word
			// This makes the intelligent instantiation much faster, by having child patterns and index into places
			// to change in the patterns all ready to go.
			this.childIndex = new int[children.size()];
			this.parentIndex = new int[children.size()];
//			childPatterns_ = new char[children_.size()][];
			for (int i = 0; i < children.size(); ++i) {
				GridWord child = children.get(i);
				Cell intersection = child.getIntersection(this.word);
				childIndex[i] = child.indexOfCellInPattern(intersection);
				parentIndex[i] = this.word.indexOfCellInPattern(intersection);
//				childPatterns_[i] = copyCharArray(child.getPattern());
			}

			// build map of Cell -> crossing word
			for (GridWord crossingWord : this.wordToCrossingWordsMap.get(this.word))
				cellToCrossingWordMap.put(this.word.getIntersection(crossingWord), crossingWord);
		}

		/** Set (copies) pattern and its matches to the specified values */
		public void setPattern(char[] pattern, List<Pair<char[], Word>> matches) {
			// if pattern matches currPattern, then we can leave choices alone
			if (copyAndCompare(pattern, currPattern))
				return;
			choices = new CircularList<Pair<char[], Word>>();

			Set<char[]> explanationKeys = explanations.keySet();
			for (Pair<char[], Word> match : matches)
				if (! explanationKeys.contains(match.first))
					choices.add(match);
		}

		public boolean canBackTrack() {
			return breadth-- > 0;
		}

		// Include not only intersecting words in explanation for failure.
		// If we reject words because intersecting words have no choices,
		// then include all the full words each of those 0-choice
		// intersecting words intersects with too
		public boolean getBestChoiceOrRollback() {
			int choicesChecked = 0;
			int choicesSize = choices.size();
			char[][] childPatterns = new char[children.size()][];

			// Update childPatterns. NOTE we copy so we don't mutate child's pattern
			//TODO If child is a fully-formed word, we don't want to destroy it here!
			//TODO figure out which children intersection cells can be written and which must not, and thereby figure out our pattern here
			for (int i = 0; i < children.size(); ++i) {
//				char[] childPat = wordToTupleMap_.get(children_.get(i)).currPattern_;
				char[] childPat = wordToTupleMap.get(children.get(i)).computePattern();
				childPatterns[i] = childPat;
//System.out.println("Child " + i + " currPattern_ = " + new String(childPat) + " vs getPattern() = " + new String(patCopy) + " vs GridWord.getPattern " + new String(children_.get(i).getPattern()));
//childPat = patCopy;
//				for (int j = 0; j < childPat.length; ++j)
//					childPatterns_[i][j] = childPat[j];
			}
			
			List<Pair<char[], Word>>[] childMatches = new List[childPatterns.length];
			// get next choice
			Set<char[]> explanationKeys = explanations.keySet();
CHOICELOOP:	while (choicesChecked++ < choicesSize) {
				Pair<char[], Word> choice = choices.getNext();
				char[] choiceWord = choice.first;
				// Check and see if this choiceWord has been eliminated
				if (explanationKeys.contains(choiceWord))
					continue;

				// Now check all its children
				for (int i = 0; i < childPatterns.length; ++i) {
					char[] pattern = childPatterns[i];
					pattern[childIndex[i]] = choiceWord[parentIndex[i]];
					childMatches[i] = dict.getPatternMatches(pattern);
					if (childMatches[i].size() == 0) {
						addChildExplanation(choice, children.get(i));
						continue CHOICELOOP;
					}
				}
//System.out.println("Setting contents " + new String(choiceWord));
				// Put best word in and update candidates for all its intersecting words, then loop back for next cheapest word choice
				word.setAutofillContents(choiceWord, savedConfig);
				
				// Update all children's patterns and matches
				for (int i = 0; i < children.size(); ++i) {
					GridWord child = children.get(i);
					WorkTuple childTuple = wordToTupleMap.get(child);
//System.out.println("Child has " + new String(childTuple.word_.getPattern()) + ", overriding with pattern " + new String(childPatterns_[i]));					
					childTuple.setPattern(childPatterns[i], childMatches[i]);
					
					if (childTuple.choices.size() == 0) {
//System.out.println("ChildTuple.choices_.size() == 0!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1");						
						//TODO need to rollback prev. work here!!!
						addChildExplanation(choice, child);
						continue CHOICELOOP;
					}
				}
				lastChoice = choice;
//System.out.println("returning true");				
				return true;
			}

			// backtrack: undo this word, restore arc consistency of neighbors
//System.out.println("undo, return false");
			undoWord();
			return false;
		}

		public void addExplanationAndUndo(Set<GridWord> gridWords) {
			// Not checking for existing explanation here because there shouldn't be one yet
			Pair<Pair<char[], Word>, Set<GridWord>> explanation =
				new Pair<Pair<char[], Word>, Set<GridWord>>(lastChoice, new HashSet<GridWord>(gridWords));
			explanations.put(lastChoice.first, explanation);

			// Remove the most recent choice from choices, and undo
			choices.deletePrev();
			undoWord();
			
			// reset pattern and choices, if necessary
			resetPattern();
		}

		/**
		 * @return explainingGridwords, all GridWord involved in explanations
		 */
		public Set<GridWord> getExplainingGridwords() {
			Set<GridWord> retval = new HashSet<GridWord>();
			for (Pair<Pair<char[], Word>, Set<GridWord>> value : explanations.values())
				retval.addAll(value.second);
			return retval;
		}

		private void addChildExplanation(Pair<char[], Word> choice, GridWord child) {
			char[] choiceWord = choice.first;
			// add explanation with child's cross words, and delete choice
			Set<GridWord> eliminatingExplanation = new HashSet<GridWord>();
			for (GridWord crossingWord : wordToCrossingWordsMap.get(child))
				if (crossingWord != word && crossingWord.isComplete())
					eliminatingExplanation.add(crossingWord);
			explanations.put(choiceWord,
					new Pair<Pair<char[], Word>, Set<GridWord>>(choice, eliminatingExplanation));
			choices.deletePrev();
		}
		
		// Remove all explanations associated with specified gridWord.
		private void removeFromExplanations(GridWord gridWord) {
			char[] currPattern = computePattern();
			for (Iterator<Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>>> it = explanations.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<char[], Pair<Pair<char[], Word>, Set<GridWord>>> entry = it.next();
				Pair<Pair<char[], Word>, Set<GridWord>> value = entry.getValue();
				if (value.second.contains(gridWord)) {
					it.remove();
					// Only add back to choices if it conforms to current pattern
					if (conformsToPattern(value.first.first, currPattern))
						choices.add(value.first);
				}
			}
		}
		
		// return whether specified value matches pattern_, expected to be of same length
		private boolean conformsToPattern(char[] value) {
			return conformsToPattern(value, currPattern);
		}

		// return whether specified value matches specified pattern, expected to be of same length (therefor length not checked here)
		private boolean conformsToPattern(char[] value, char[] pattern) {
			for (int i = 0; i < pattern.length; ++i)
				if (pattern[i] != '_' && pattern[i] != value[i])
					return false;
			return true;
		}

		// clear chars from word, except those involved in complete cross words, reset affected child's patterns
		private void undoWord() {
			for (Cell cell : word.getCells()) {
				GridWord crossWord = cellToCrossingWordMap.get(cell);
				if (! crossWord.isComplete()) {
					// Clear cell, reset child pattern and matches
					cell.setEmpty();
					wordToTupleMap.get(crossWord).resetPattern();
				}
			}
//			currPattern_ = computePattern();
		}

		// reset currPattern and choices, excluding choices in explanations
		public void resetPattern() {
			choices = new CircularList<Pair<char[], Word>>();
			currPattern = computePattern();

			// exclude anything in an explanation
			Set<char[]> explanationKeys = explanations.keySet();
			for (Pair<char[], Word> match : dict.getPatternMatches(currPattern))
				if (! explanationKeys.contains(match.first))
					choices.add(match);
		}

		// return pattern for word, taking into account which intersecting words
		// are completed. Assumes pattern length will not change. Returns new array
		private char[] computePattern() {
			char[] retval = new char[currPattern.length];
			int index = 0;
			for (Cell cell : word.getCells()) {
				//TODO Is it right to allow autofill on already filled cells here,
				// merely because the crossing word is incomplete??
				if (cell.isEligibleForAutofill() || ! cellToCrossingWordMap.get(cell).isComplete()) {
					retval[index++] = '_';
				} else {
					cell.appendContents(retval, index);
					index += cell.getContentsSize();
				}
			}
			return retval;
		}

		// copy source to dest and return true if they were already equal
		private boolean copyAndCompare(char[] src, char[] dest) {
			boolean retval = true;
			for (int i = 0; i < src.length; ++i)
				if (dest[i] != src[i]) {
					dest[i] = src[i];
					retval = false;
				}
			return retval;
		}

		// return a copy of the specified char[]
		private char[] copyCharArray(char[] input) {
			char[] retval = new char[input.length];
			for (int i = 0; i < input.length; ++i)
				retval[i] = input[i];
			return retval;
		}
	}
}
