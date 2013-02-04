/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.stangl.crostex.GridWord.Direction;
import us.stangl.crostex.util.CircularList;
import us.stangl.crostex.util.Stack;

/**
 * 5th generation implementation of auto-fill algorithm.
 */
public class AutoFiller5 implements AutoFill {
	public boolean autoFill(Grid grid, Dictionary<char[], Word> dict) {
        // Find all full words in xword, store in set for duplicate checking
		Set<String> wordsAlreadyInUse = new HashSet<String>();
		List<GridWord> acrossWordsToFill = new ArrayList<GridWord>();
		List<GridWord> downWordsToFill = new ArrayList<GridWord>();
		Map<GridWord, Pair<Cell[], int[]>> originalValueStore = new HashMap<GridWord, Pair<Cell[], int[]>>();
		
		for (GridWord gw : grid.getAcrossWords()) {
			if (gw.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(gw.getPattern()))
					return false;						// cannot satisfy this across word
				acrossWordsToFill.add(gw);
				originalValueStore.put(gw, gw.getFillConfig());
			} else if (gw.isComplete()) {
				wordsAlreadyInUse.add(gw.getContents());
			}
		}
		
		for (GridWord gw : grid.getDownWords()) {
			if (gw.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(gw.getPattern()))
					return false;						// cannot satisfy this down word
				downWordsToFill.add(gw);
				originalValueStore.put(gw, gw.getFillConfig());
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

        // Generate array of references from each partial down word to
        // partial across words they intersect with a blank
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
		originalValueStore = null;
		wordsAlreadyInUse = null;

		// Build a Map from every GridWord -> its list of candidates
		Map<GridWord, List<Pair<char[], Word>>> wordPossibilities = new HashMap<GridWord, List<Pair<char[], Word>>>();
		
		for (GridWord word : wordToCrossingWordsMap.keySet())
			wordPossibilities.put(word, dict.getPatternMatches(word.getPattern()));
		
		Stack<WorkTuple> workStack = new Stack<WorkTuple>();
		
MAINLOOP:
		while (true) {
			// Now find "cheapest" candidate to work next, the one with the fewest number of possibilities
			int lowestNumChoicesSoFar = Integer.MAX_VALUE;
			GridWord cheapestWord = null;
			for (Map.Entry<GridWord, List<Pair<char[], Word>>> entry : wordPossibilities.entrySet()) {
				GridWord word = entry.getKey();
				if (word.isComplete())
					continue;
				int size = entry.getValue().size();
//				if (size == 0) {
//					// We would normally need to backtrack here, but since it's our initial foray, we conclude grid cannot be filled
//					return false;
//				}
				if (size < lowestNumChoicesSoFar) {
					lowestNumChoicesSoFar = size;
					cheapestWord = word;
				}
			}
			
			if (cheapestWord == null)
				return true;					// Done!

			// Create new work tuple for this word, and put it into map/stack if it yields any good choices
			WorkTuple workTuple = new WorkTuple(cheapestWord, wordPossibilities, dict, wordToCrossingWordsMap);
			
			if (workTuple.getBestChoiceOrRollback()) {
				workStack.push(workTuple);
			} else {
				// Backtrack here: Work backwards up work stack looking for an intersecting word that we can we change
				while (! workStack.empty()) {
					workTuple = workStack.pop();
					GridWord prevWord = workTuple.word_;
					
//					// TODO if we do multiple levels of backtracking, we should maybe check cell more closely
					Cell intersection = prevWord.getIntersection(cheapestWord);
					if (intersection != null) {
						if (workTuple.getNextBestChoice(intersection)) {
							workStack.push(workTuple);
							continue MAINLOOP;
						}

						cheapestWord = prevWord;
					} else {
						// Restore this word
						prevWord.restoreFromFillConfig(workTuple.savedConfig_);

						// Restore surrounding children possibilities
						for (GridWord child : workTuple.children_)
							wordPossibilities.put(child, dict.getPatternMatches(child.getPattern()));
					}
				}
				return false;				// We backtracked all the way out and couldn't find a solution
			}
		}
	}
	

	private static class WorkTuple {
		// Maximum number of choices to lookahead to to determine which has best impact on its children
		private static final int MAX_NUM_CHOICES = 10;
		
		public final GridWord word_;
		
		public Pair<Cell[], int[]> savedConfig_;
		
		public List<GridWord> children_;
		
		private Map<GridWord, List<Pair<char[], Word>>> wordPossibilities_;
		
		private CircularList<Pair<char[], Word>> choices_ = new CircularList<Pair<char[], Word>>();
		
		private Dictionary<char[], Word> dict_;
		
		/** index into each member of children pattern, of intersection with word_ */
		private final int[] childIndex_;
		
		/** index into word_ pattern, of intersection with each corresponding member of children_ */
		private final int[] parentIndex_;
		
		/** children patterns */
		private final char[][] childPatterns_;
		
		public WorkTuple(GridWord word, Map<GridWord, List<Pair<char[], Word>>> wordPossibilities, Dictionary<char[], Word> dict,
				Map<GridWord, List<GridWord>> wordToCrossingWordsMap)
		{
			word_ = word;
			wordPossibilities_ = wordPossibilities;
			dict_ = dict;
			savedConfig_ = word.getFillConfig();
			choices_.addAll(wordPossibilities.get(word));

			// get list of only the children that need filling
			List<GridWord> allChildren = wordToCrossingWordsMap.get(word_);
			children_ = new ArrayList<GridWord>(allChildren.size());
			for (GridWord child : allChildren)
				if (! child.isComplete())
					children_.add(child);
			
			// for each child, store index in child of intersection with our word, and index in our word
			// This makes the intelligence instantiation much faster, by having child patterns and index into places
			// to change in the patterns all ready to go.
			childIndex_ = new int[children_.size()];
			parentIndex_ = new int[children_.size()];
			childPatterns_ = new char[children_.size()][];
			for (int i = 0; i < children_.size(); ++i) {
				GridWord child = children_.get(i);
				Cell intersection = child.getIntersection(word_);
				childIndex_[i] = child.indexOfCellInPattern(intersection);
				parentIndex_[i] = word_.indexOfCellInPattern(intersection);
				childPatterns_[i] = child.getPattern();
			}
		}

		public boolean getNextBestChoice(Cell avoidCell) {
			char cellContents = avoidCell.getContents().charAt(0);
			int patternIndex = word_.indexOfCellInPattern(avoidCell);

			long maxChildProduct = 0;
			int nonZeroChoicesChecked = 0;
			int choicesChecked = 0;
			int choicesSize = choices_.size();
			Pair<char[], Word> bestChoice = null;

CHOICELOOP:	
//			for (int choiceIndex = 0; nonZeroChoicesChecked < MAX_NUM_CHOICES && choiceIndex < choicesSize; ++choiceIndex) {
			// Checking against choicesSize so we can detect when we have wrapped around
			while (nonZeroChoicesChecked < MAX_NUM_CHOICES && choicesChecked++ < choicesSize) {
				Pair<char[], Word> choice = choices_.getNext();
				char[] choiceWord = choice.first_;
//				word_.setAutofillContents(choice.first_, savedConfig_);
//				if (avoidCell != null && avoidCell.getContents().equals(cellContents))
					
				if (choiceWord[patternIndex] == cellContents)
					continue CHOICELOOP;
					
				long childProduct = 1L;
				// Now check all its children
				for (int i = 0; i < childPatterns_.length; ++i) {
//				for (GridWord child : children_) {
					char[] pattern = childPatterns_[i];
					pattern[childIndex_[i]] = choiceWord[parentIndex_[i]];
					int childSize = dict_.getPatternMatches(pattern).size();
					if (childSize == 0) {
						// delete previous one.
						choices_.deletePrev();
						continue CHOICELOOP;
					}
					childProduct *= childSize;
				}
				
				if (childProduct > maxChildProduct) {
					// Remove this choice from circular list.
					choices_.deletePrev();
					maxChildProduct = childProduct;
					if (bestChoice != null)
						choices_.add(bestChoice);			// restore previous best choice back onto end of list
					bestChoice = choice;
				}
				++nonZeroChoicesChecked;
			}
			
			if (bestChoice == null) {
				// backtrack
// COMMENTED OUT FOLLOWING LINES SINCE IF THIS FAILS WE CALL GetBestChoiceOrRollback				
//				word_.restoreFromFillConfig(savedConfig_);
//				for (GridWord child : children_)
//					wordPossibilities_.put(child, dict_.getPatternMatches(child.getPattern()));
				return false;
			}
			
			// Put best word in and update candidates for all its intersecting words, then loop back for next cheapest word choice
			word_.setAutofillContents(bestChoice.first_, savedConfig_);
			for (GridWord child : children_)
				wordPossibilities_.put(child, dict_.getPatternMatches(child.getPattern()));
			
			return true;
		}

	
		public boolean getBestChoiceOrRollback() {
			long maxChildProduct = 0;
			int nonZeroChoicesChecked = 0;
			int choicesChecked = 0;
			int choicesSize = choices_.size();
			Pair<char[], Word> bestChoice = null;

	CHOICELOOP:	
			// Checking against choicesSize so we can detect when we have wrapped around
			while (nonZeroChoicesChecked < MAX_NUM_CHOICES && choicesChecked++ < choicesSize) {
				Pair<char[], Word> choice = choices_.getNext();
				char[] choiceWord = choice.first_;
				
				long childProduct = 1L;
				// Now check all its children
				for (int i = 0; i < childPatterns_.length; ++i) {
					char[] pattern = childPatterns_[i];
					pattern[childIndex_[i]] = choiceWord[parentIndex_[i]];
					int childSize = dict_.getPatternMatches(pattern).size();
					if (childSize == 0) {
						// delete previous one.
						choices_.deletePrev();
						continue CHOICELOOP;
					}
					childProduct *= childSize;
				}
				
				if (childProduct > maxChildProduct) {
					// Remove this choice from circular list.
					choices_.deletePrev();
					maxChildProduct = childProduct;
					if (bestChoice != null)
						choices_.add(bestChoice);			// restore previous best choice back onto end of list
					bestChoice = choice;
				}
				++nonZeroChoicesChecked;
			}
			
			if (bestChoice == null) {
				// backtrack
				word_.restoreFromFillConfig(savedConfig_);
				for (GridWord child : children_)
					wordPossibilities_.put(child, dict_.getPatternMatches(child.getPattern()));
				return false;
			}
			
			// Put best word in and update candidates for all its intersecting words, then loop back for next cheapest word choice
			word_.setAutofillContents(bestChoice.first_, savedConfig_);
			for (GridWord child : children_)
				wordPossibilities_.put(child, dict_.getPatternMatches(child.getPattern()));
			
			return true;
		}
	}
}
