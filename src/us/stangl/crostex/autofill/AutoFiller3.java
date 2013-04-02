/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.autofill;

import java.util.ArrayList;
import java.util.Collections;
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
import us.stangl.crostex.util.Pair;

/**
 * 3rd generation grid autofill algorithm.
 * @author Alex Stangl
 */
public class AutoFiller3 implements AutoFillGrid {
	public boolean autoFill(Grid grid, Dictionary<char[], Word> dict) {
        // Find all full words in xword, store in set for duplicate checking
		Set<String> wordsAlreadyInUse = new HashSet<String>();
		List<GridWord> acrossWords = grid.getAcrossWords();
		List<GridWord> downWords = grid.getDownWords();
		List<GridWord> acrossWordsToFill = new ArrayList<GridWord>();
		List<GridWord> downWordsToFill = new ArrayList<GridWord>();
		Map<GridWord, Pair<Cell[], int[]>> originalValueStore = new HashMap<GridWord, Pair<Cell[], int[]>>();
		
//		System.out.println("acrossWords = ");
		for (GridWord gw : acrossWords) {
//System.out.println(gw);
			if (gw.isEligibleForAutofill()) {
				if (! dict.isPatternInDictionary(gw.getPattern()))
					return false;						// cannot satisfy this across word
				acrossWordsToFill.add(gw);
				originalValueStore.put(gw, gw.getFillConfig());
			} else if (gw.isComplete()) {
				wordsAlreadyInUse.add(gw.getContents());
			}
		}
		
//System.out.println("downWords = ");
		for (GridWord gw : downWords) {
//System.out.println(gw);
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
		Map<GridWord, List<GridWord>> acrossToDownWordMap = new HashMap<GridWord, List<GridWord>>();
		Map<GridWord, List<GridWord>> wordToCrossingWordsMap = new HashMap<GridWord, List<GridWord>>();
		for (GridWord agw : acrossWordsToFill) {
			List<GridWord> list = new ArrayList<GridWord>();
			for (GridWord dgw : downWordsToFill) {
				Cell intersection = agw.getIntersection(dgw);
				if (intersection != null && intersection.isEligibleForAutofill()) {
//System.out.println("associating across word " + agw + " with down word " + dgw);					
					list.add(dgw);
				}
			}
			acrossToDownWordMap.put(agw, list);
			wordToCrossingWordsMap.put(agw, list);
//System.out.println("Putting in wordMap " + agw + " " + list.size() + " words");
//for (GridWord word : wordToCrossingWordsMap.keySet())
//	System.out.println("In keyset now: " + word);
		}

        // Generate array of references from each partial down word to
        // partial across words they intersect with a blank
		Map<GridWord, List<GridWord>> downToAcrossWordMap = new HashMap<GridWord, List<GridWord>>();
		for (GridWord dgw : downWordsToFill) {
			List<GridWord> list = new ArrayList<GridWord>();
			for (GridWord agw : acrossWordsToFill) {
				Cell intersection = dgw.getIntersection(agw);
				if (intersection != null && intersection.isEligibleForAutofill()) {
					list.add(agw);
//System.out.println("associating down word " + dgw + " with across word " + agw);					

				}
			}
			downToAcrossWordMap.put(dgw, list);
			wordToCrossingWordsMap.put(dgw, list);
//System.out.println("Putting in wordMap " + dgw + " " + list.size() + " words");			
//for (GridWord word : wordToCrossingWordsMap.keySet())
//	System.out.println("In keyset now: " + word);
		}

//        // For the N across words, do an effective nested for loop,
//        // nested N levels deep, iterating through the trie iterator for
//        // each across pattern. Net effect is to work through all across permutations
//		// First word iterates through once
//		// Second word iterates through once, then lets first word iterate to next, then iterates through again
//		
//		// So, last word iterates most often. Each time it completes an iteration, bump the one before it and reset it
//
//		List<ResettableIterator<Pair<char[], Word>>> iteratorList = new ArrayList<ResettableIterator<Pair<char[], Word>>>(acrossWordsToFill.size());
//		for (GridWord gw : acrossWordsToFill) {
//			ResettableIterator<Pair<char[], Word>> it = dict.getIterator(gw.getPattern());
//			if (! it.hasNext())
//				// Cannot fill this!
//				return false;
//			iteratorList.add(it);
//		}
		
		// Seems like we should have 2 phases here.
		// First phase, start populating each partial across words, starting from top, checking intersecting DOWN words,
		// and incrementing iterator, working backwards as necessary.
		// This phase ends prematurely if unable to autofill, or with first found autofill.
		
		// Second phase involves sweeping through the remaining permutations of iterators, to check all the remaining autofill
		// possibilities.
		
		// FIRST, focus on the first phase, to check out performance of autofill and get some gratification!!!! 
		
		// Create DAG of across words, with each across word pointing to 
//		int highestWordNumber = 1 + (acrossWords.size() > downWords.size() ? acrossWords.size() : downWords.size());
		
//		int highestWordNumber = acrossWords.size() + downWords.size();
////		1 + (acrossWords.size() > downWords.size() ? acrossWords.size() : downWords.size());
//		
//		// To track which words already in use on the stack
//		System.out.println("creating wordtracker sized " + highestWordNumber * 2 + 2);
//		boolean[] wordTracker = new boolean[highestWordNumber * 2 + 2];
		
/*
		GridWord gridWord = acrossWordsToFill.get(0);
		//List<Pair<GridWord, Integer>> intersectingDownWords = acrossToDownWordMap.get(gridWord);
		for (Pair<GridWord, Integer> pair : acrossToDownWordMap.get(gridWord)) {
			System.out.println("pair.first_ = " + pair.first_ + ", second_ = " + pair.second_);
			System.out.println("word number = " + pair.first_.getNumber() + ", direction = " + pair.first_.getDirection());
		}
		return false;
	*/	
		GridWord firstWord = acrossWordsToFill.get(0);
		
		// free up things we don't really need
		downToAcrossWordMap = null;
		acrossToDownWordMap = null;
		wordsAlreadyInUse = null;
		acrossWords = null;
		downWords = null;
		return populateWord3(firstWord, wordToCrossingWordsMap, originalValueStore, dict, dict.getPatternMatches(firstWord.getPattern()), Collections.EMPTY_LIST);

//		return populateWord(firstWord, wordToCrossingWordsMap, originalValueStore, dict, dict.getPatternMatches(firstWord.getPattern()));

//		return populateWord(acrossWordsToFill.get(0), acrossToDownWordMap, downToAcrossWordMap, originalValueStore, dict, wordTracker);
		
//		return populateAcrossWord(0, acrossWordsToFill, iteratorList, acrossToDownWordMap, originalValueStore, dict);

		/*
		int index = 0;
		GridWord gridWord = acrossWordsToFill.get(index);
		ResettableIterator<TrieTuple> it = iteratorList.get(index);
		List<GridWord> intersectingDownWords = acrossToDownWordMap.get(gridWord);
		while (it.hasNext()) {
			TrieTuple tuple = it.next();
			saveNewWordIntoGrid(acrossWordsToFill.get(index), tuple, originalValueStore);
			// Now check intersecting down words to make sure they're all still OK
			if (checkGridWordPatternsAllInTrie(intersectingDownWords, trie)) {
				
			}
		}
		
		
		// Skipping words-in-use check here. It is enormously complicating the selection logic. Better to just ignore duplicates here,
		// and if we get an otherwise valid full fill, then check them all for duplicates then, and if any found, then reject that
		// fill and try again.
		index = acrossWordsToFill.size() - 1;
		while (index >= 0) {
			// Should take word out of words to use first here. Not going to worry too much about exact correctness for now though
			ResettableIterator<TrieTuple> it = iteratorList.get(index);
			if (it.hasNext()) {
				TrieTuple tuple = it.next();

				// OK, we finally successfully bumped one. Now reset every other iterator after and get words from them
				// Save tuple into across word
				saveNewWordIntoGrid(acrossWordsToFill.get(index), tuple, originalValueStore);
				for (int sub = index + 1; sub < acrossWordsToFill.size(); ++ sub) {
					ResettableIterator<TrieTuple> itSub = iteratorList.get(sub);
					itSub.reset();
					TrieTuple subTuple = it.next();
					
					// Save tuple into across word
					saveNewWordIntoGrid(acrossWordsToFill.get(sub), subTuple, originalValueStore);
				}
			}
		}

        // For each tentative across word returned from trie,
        //   check if it is in duplicate set. If so, discard it and
        //   try another. If it isn't add it to set for the duration of
        //   when we are considering it, and then remove it afterwards.

        // Each time an across word changes, besides removing its prev.
        //   value from the duplicate set, and adding new value to the set,
        //   check all affected down word patterns. If any of the affected
        //   down word patterns now has no possibilities, discard this
        //   across word.

		// WHAT DOES THIS PP ABOVE MEAN?!?  If we select a new across word for the first word, should the DOWN word include all the old across
		// words below it, or wildcards for those??  Seems like it should be the latter -- for DOWN word checks, all downstream across words
		// should be restored to their original values first
		
        // For each full set of autofilled across words, down words should
        //   all map too. Add up the scores for all the autofilled words.
        //   Compare to best score so far. If better than best so far, make
        //   it the new best.

        // After working through all permutations, use best one found.


	*/
	}

	private boolean populateWord3(GridWord gridWord,
			Map<GridWord, List<GridWord>> wordToCrossingWordsMap,
			Map<GridWord, Pair<Cell[], int[]>> originalValueStore,
			Dictionary<char[], Word> dict,
			List<Pair<char[], Word>> candidates,
			List<GridWord> otherWordsDelegated)
	{
		// Get list of words currently crossing gridWord at blank cells partitioned into 2 collections:
		// crossWordsAlmostComplete, words whose only empty cell is the intersecting cell, and
		// crossWordsNeedingMoreWork, words which have other empty cells besides the one intersecting with gridWord
		// words that intersect gridWord at non-empty cells are ignored, as they need no further attention
		List<GridWord> crossWords = wordToCrossingWordsMap.get(gridWord);
		List<GridWord> crossWordsAlmostComplete = new ArrayList<GridWord>(crossWords.size());
		List<GridWord> crossWordsNeedingWork = new ArrayList<GridWord>(crossWords.size());
		for (GridWord crossWord : crossWords) {
			Cell intersection = gridWord.getIntersection(crossWord);
			if (intersection.isEmpty()) {
				if (crossWord.getNumberCellsRequiringAutofill() == 1)
					crossWordsAlmostComplete.add(crossWord);
				else
					crossWordsNeedingWork.add(crossWord);
			}
		}

		// Save current config, to restore afterwords, if we fail
		Pair<Cell[], int[]> fillConfig = gridWord.getFillConfig();
		
AcrossWord:
		for (Iterator<Pair<char[], Word>> it = candidates.iterator(); it.hasNext(); ) {
			
			// Try candidate
			Pair<char[], Word> tuple = it.next();
			gridWord.setAutofillContents(tuple.first, fillConfig);
			
			// First, check all intersecting words completed by this word, making sure they are in dictionary. If not, try another candidate.
			for (GridWord crossWord : crossWordsAlmostComplete)
				if (dict.lookup(crossWord.getContents().toCharArray()) == null)
					continue AcrossWord;

			// Now check crossWordsNeedingMoreWork in succession. Take the one with the fewest possibilities and try
			// to recursively solve it. If solved, try another until there all cross words solved, or one cannot be solved, in which case, try another candidate.
			// Keep checking all intersecting words until they're all good, or we decide they cannot be satisfied
			CrossWordPossibilitiesTuple crossTuple = findNextWork(crossWordsNeedingWork, dict);
			if (crossTuple == null)
				// intersecting word cannot be filled, try another candidate
				continue AcrossWord;

			if (crossTuple.gridWord_ != null) {
				crossWordsNeedingWork.remove(crossTuple.gridWord_);
				List<GridWord> workRemaining = new ArrayList<GridWord>(crossWordsNeedingWork);
				workRemaining.addAll(otherWordsDelegated);
				if (! populateWord3(crossTuple.gridWord_, wordToCrossingWordsMap, originalValueStore, dict, crossTuple.matches_, crossWordsNeedingWork)){
					// One of the other words we were delegated could not be completed. D'oh!! Rollback our current work, and return failure.
					gridWord.restoreFromFillConfig(fillConfig);
					return false;
				}
				return true;
			}
			
			// OK, everything looks good so far.
			// Success! Now work on any other work we were delegated.
			if (otherWordsDelegated.size() == 0)
				return true;								// nothing else to do!
			
			crossTuple = findNextWork(otherWordsDelegated, dict);
			if (crossTuple == null) {
				// One of the other words we were delegated could not be completed. D'oh!! Rollback our current work, and return failure.
				gridWord.restoreFromFillConfig(fillConfig);
				return false;
			} else {
				crossWordsNeedingWork.remove(crossTuple.gridWord_);
				if (! populateWord3(crossTuple.gridWord_, wordToCrossingWordsMap, originalValueStore, dict, crossTuple.matches_, crossWordsNeedingWork))
					// Cannot satisfy the intersecting words
					continue AcrossWord;
				return true;
			}
			
			
//			// NOTE: If we go to another candidate after children have done work, we should really rollback their work!!!
//			if (crossWordToDrillDown == null)
//				// everything looks OK, now check any other work which we were delegated
//				return true;
//
//			if (! populateWord3(crossWordToDrillDown, wordToCrossingWordsMap, originalValueStore, dict, smallestListOfMatches, crossWordsNeedingWork))
//				// Cannot satisfy the intersecting words
//				continue AcrossWord;
//			return true;
		}
		gridWord.restoreFromFillConfig(fillConfig);
		return false;
	}
	
	/**
	 * Check collection of words for remaining work to do.
	 * Return null if cannot complete words, or else CrossWordPossibilitiesTuple saying either work is done, or pointing to next thing to do
	 * If tuple contains null gridWord, there is nothing left to do.
	 * @param wordsNeedingWork collection of words needing work
	 * @param dict dictionary
	 * @return null if cannot complete words, or else CrossWordPossibilitiesTuple saying either work is done, or pointing to next thing to do
	 */
	private CrossWordPossibilitiesTuple findNextWork(List<GridWord> wordsNeedingWork, Dictionary<char[], Word> dict) {
		int smallestListSize = Integer.MAX_VALUE;
		List<Pair<char[], Word>> smallestListOfMatches = null;
		GridWord bestWord = null;

		for (GridWord word : wordsNeedingWork) {
			if (! word.isEligibleForAutofill()) {
//System.out.println("Skipping " + word.getNumber() + " " + word.getDirection() + " " + word.getContents() + " because it's already complete.");				
				continue;							// word already completed, skip it
			}
			char[] pattern = word.getPattern();
			List<Pair<char[], Word>> matches = dict.getPatternMatches(pattern);
//System.out.println("word = " + word.getNumber() + " " + word.getDirection() + " " + word.getContents() + ", matches size = " + matches.size() + " for pattern " + new String(pattern));						
//			System.out.println("index = " + intersectingWordIndex + ", candidate = " + new String(tuple.first_) + ", matches size = " + matches.size() + " for pattern " + new String(intersectPattern));						
			if (matches.size() == 0)
				// we cannot complete a word, return failure
				return null;
				
			if (matches.size() < smallestListSize) {
				smallestListSize = matches.size();
				smallestListOfMatches = matches;
				bestWord = word;
			}
		}
//System.out.println("Returning best word to pursue = " + (bestWord == null ? "null" : bestWord.getContents()) + ", with " + smallestListSize + " matches.");		
		return new CrossWordPossibilitiesTuple(bestWord, smallestListOfMatches);
	}
	
	// if gridWord_ == null, we're done
	private static class CrossWordPossibilitiesTuple {
		/** GridWord to focus on next */
		public final GridWord gridWord_;
		
		/** matches to check for GridWord */
		public final List<Pair<char[], Word>>  matches_;
		
		public CrossWordPossibilitiesTuple(GridWord gridWord, List<Pair<char[], Word>> matches) {
			gridWord_ = gridWord;
			matches_ = matches;
		}
	}
}
