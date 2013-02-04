/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import us.stangl.crostex.util.ResettableIterator;

/**
 * 0th generation grid auto-fill algorithm.
 */
public class AutoFiller0 implements AutoFill {
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
		List<ResettableIterator<Pair<char[], Word>>> iteratorList = new ArrayList<ResettableIterator<Pair<char[], Word>>>(acrossWordsToFill.size());
		for (GridWord gw : acrossWordsToFill) {
			ResettableIterator<Pair<char[], Word>> it = dict.getIterator(gw.getPattern());
			if (! it.hasNext())
				// Cannot fill this!
				return false;
			iteratorList.add(it);
		}
		
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
//		GridWord firstWord = acrossWordsToFill.get(0);
		
		// free up things we don't really need
		downToAcrossWordMap = null;
		acrossToDownWordMap = null;
		originalValueStore = null;
		wordsAlreadyInUse = null;
		acrossWords = null;
		downWords = null;

		//		dict.getPatternMatches(firstWord.getPattern());
//		return populateWord(firstWord, wordToCrossingWordsMap, originalValueStore, dict, dict.getPatternMatches(firstWord.getPattern()));

//		return populateWord(acrossWordsToFill.get(0), acrossToDownWordMap, downToAcrossWordMap, originalValueStore, dict, wordTracker);
		
//		return populateAcrossWord(0, acrossWordsToFill, iteratorList, acrossToDownWordMap, originalValueStore, dict);
		return false;
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

	private boolean populateAcrossWord(int index,
			List<GridWord> acrossWordsToFill,
			List<ResettableIterator<Pair<char[], Word>>> iteratorList,
			Map<GridWord, List<Pair<GridWord, Integer>>> acrossToDownWordMap,
			Map<GridWord, Pair<Cell[], int[]>> originalValueStore,
			Dictionary<char[], Word> dict)
	{
//		if (index >= acrossWordsToFill.size())
//			return true;							// We've hit the bottom, so all is good
		GridWord gridWord = acrossWordsToFill.get(index);
		ResettableIterator<Pair<char[], Word>> it = iteratorList.get(index);
		List<Pair<GridWord, Integer>> intersectingDownWords = acrossToDownWordMap.get(gridWord);
		it.reset();
		while (it.hasNext()) {
			Pair<char[], Word> tuple = it.next();
			saveNewWordIntoGrid(gridWord, tuple, originalValueStore);
			// Now check intersecting down words to make sure they're all still OK
			if (checkGridWordPatternsAllInTrie(intersectingDownWords, dict, it)) {
				if (index + 1 >= acrossWordsToFill.size())
					return true;
				// Try to fill in across words below
				if (populateAcrossWord(index + 1, acrossWordsToFill, iteratorList, acrossToDownWordMap, originalValueStore, dict))
					return true;
				
//				// Reset all the iterators for every across word below
//				for (int index2 = index + 1; index2 < acrossWordsToFill.size(); ++index2)
//					iteratorList.get(index2).reset();
			}
		}
		// We've run out of word candidates with none meeting the criteria. Restore GridWord and return failure

		//TODO Use a DAG or other dependency mapping to backtrack to previous across words that need to change,
		//     and leave ones alone that are still OK for now.
//		gridWord.restoreFromMap(originalValueStore.get(gridWord));
		gridWord.restoreFromFillConfig(originalValueStore.get(gridWord));
		return false;
	}
	
	/** return boolean indicating whether the patterns for all the GridWords in the collection are in the trie */
	private boolean checkGridWordPatternsAllInTrie(Collection<Pair<GridWord, Integer>> gridWords,
			Dictionary<char[], Word> dict,
			ResettableIterator<Pair<char[], Word>> acrossIt) {
		// Now check intersecting down words to make sure they're all still OK
		for (Pair<GridWord, Integer> gridWordPair : gridWords) {
			if (! dict.isPatternInDictionary(gridWordPair.first_.getPattern())) {
				acrossIt.avoidLetterAt(gridWordPair.second_);
				
				// Tried not breaking out of loop immediately here, but it seemed to reduce performance.
				return false;
			}
		}
		return true;
	}

//	private void saveNewWordIntoGrid(GridWord gridWord, Pair<char[], Word> tuple, Map<GridWord, Map<Cell, String>> oldValueStore) {
	private void saveNewWordIntoGrid(GridWord gridWord, Pair<char[], Word> tuple, Map<GridWord, Pair<Cell[], int[]>> oldValueStore) {
		// First restore previous value, if there was one.
//		Map<Cell, String> prevValue = oldValueStore.get(gridWord);
//		gridWord.restoreFromMap(prevValue);
		gridWord.setAutofillContents(tuple.first_, oldValueStore.get(gridWord));
	}
	
//	/** return usable word from this iterator, if there is one, else null */ 
//	private TrieTuple getAvailableWordFromIterator(ResettableIterator<TrieTuple> it, Set<String> wordsAlreadyInUse) {
//		while (true) {
//			if (! it.hasNext())
//				return null;
//			TrieTuple tuple = it.next();
//			String wordText = new String(tuple.text_);
////			if (! wordsAlreadyInUse.contains(wordText))
//				return tuple;
//		}
//	}
}
