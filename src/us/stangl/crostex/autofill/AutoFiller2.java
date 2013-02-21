/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.autofill;

import java.util.ArrayList;
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
 * 2nd generation grid auto-fill algorithm.
 */
public class AutoFiller2 implements AutoFill {
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
		originalValueStore = null;
		wordsAlreadyInUse = null;
		acrossWords = null;
		downWords = null;
		return populateWord(firstWord, wordToCrossingWordsMap, originalValueStore, dict, dict.getPatternMatches(firstWord.getPattern()));

		//		dict.getPatternMatches(firstWord.getPattern());
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

	private boolean populateWord(GridWord gridWord,
			Map<GridWord, List<GridWord>> wordToCrossingWordsMap,
			Map<GridWord, Pair<Cell[], int[]>> originalValueStore,
			Dictionary<char[], Word> dict,
			List<Pair<char[], Word>> candidates)
	{
		List<GridWord> intersectingWords = wordToCrossingWordsMap.get(gridWord);
		List<GridWord> intersectingWordsOfInterest = new ArrayList<GridWord>(intersectingWords.size());
		for (GridWord intersectingWord : intersectingWords) {
			Cell intersection = gridWord.getIntersection(intersectingWord);
			if (intersection.isEmpty())
				intersectingWordsOfInterest.add(intersectingWord);
		}

		// alreadyChecked array used below to skip over elements already checked w/o having to mutate the list
		boolean[] alreadyChecked = new boolean[intersectingWordsOfInterest.size()];
		
		Pair<Cell[], int[]> fillConfig = gridWord.getFillConfig();
//		Map<String, List<Pair<char[], Word>>> intersectPatternCache = new HashMap<String, List<Pair<char[], Word>>>();
		List<Pair<Character, GridWord>> previouslyChecked = new ArrayList<Pair<Character, GridWord>>();
AcrossWord:
	// TODO cache word patterns from previous time
	//TODO do not recheck any intersecting words that have same pattern as last time -- need to be careful though. Can probably only do it
	//     in same order as previous time. Once we hit a divergence, all bets are off! Descendent squares may be populated differently, breaking assumptions
//		for (ResettableIterator<Pair<char[], Word>> it = dict.getIterator(gridWord.getPattern()); it.hasNext(); ) {
		for (Iterator<Pair<char[], Word>> it = candidates.iterator(); it.hasNext(); ) {
			Pair<char[], Word> tuple = it.next();
			gridWord.setAutofillContents(tuple.first, fillConfig);
			
			// Clear alreadyChecked flags
			int numToCheck = alreadyChecked.length;
			for (int i = 0; i < numToCheck; ++i)
				alreadyChecked[i] = false;

			// Need to copy intersectingWordsOfInterest since we remove elements below, as we check them
//			List<GridWord> intersectingWordsToCheck = new ArrayList<GridWord>(intersectingWordsOfInterest);
			
			int previouslyCheckedIndex = 0;
			boolean examinePreviouslyChecked = true;
			
			// Keep checking all intersecting words until they're all good, or we decide they cannot be satisfied
			while (numToCheck > 0) {
				int smallestListSize = Integer.MAX_VALUE;
				int smallestListIndex = -1;
				List<Pair<char[], Word>> smallestListOfMatches = null;
				int wordsOfInterestSize = intersectingWordsOfInterest.size();
				for (int intersectingWordIndex = 0; intersectingWordIndex < wordsOfInterestSize; ++intersectingWordIndex) {
					if (alreadyChecked[intersectingWordIndex])
						continue;
					
	//			for (GridWord intersectingWord : intersectingWords) {
					GridWord intersectingWord = intersectingWordsOfInterest.get(intersectingWordIndex);
//TODO next line can be commented-out/removed
//					Cell intersection = gridWord.getIntersection(intersectingWord);
//System.out.println("Examining intersection: " + intersection.isEmpty());					
//					if (intersection.isEmpty()) {
					char[] intersectPattern = intersectingWord.getPattern();

					// TODO need to rethink this wildcard check -- what we really want to test here, why, and if it is fully safe
					// Doesn't seem like we can safely overlook the intersecting word simply because it no longer has any blanks -- we may
					// have created a nonsense word with the last letter we have just supplied.
//					boolean wildcardFound = false;
//					for (int i = 0; i < intersectPattern.length; ++i) {
//						if (intersectPattern[i] == TstNew.WILDCARD) {
//							wildcardFound = true;
//							break;
//						}
//					}
//					if (! wildcardFound)
//						continue;
					List<Pair<char[], Word>> matches = dict.getPatternMatches(intersectPattern);
//System.out.println("index = " + intersectingWordIndex + ", candidate = " + new String(tuple.first_) + ", matches size = " + matches.size() + " for pattern " + new String(intersectPattern));						
					if (matches.size() == 0)
						// intersecting word cannot be filled, try another candidate
						continue AcrossWord;
					if (matches.size() < smallestListSize) {
						smallestListSize = matches.size();
						smallestListIndex = intersectingWordIndex;
						smallestListOfMatches = matches;
					}
				}
				
//System.out.println("smallestListOfMatches = " + smallestListOfMatches);
				if (smallestListOfMatches == null)
					// No intersecting words left to fill
					return true;

				GridWord intersectingWordOfInterest = intersectingWordsOfInterest.get(smallestListIndex);
				
				boolean skipChildCheck = false;
				if (examinePreviouslyChecked && previouslyCheckedIndex < previouslyChecked.size()) {
					char intersectChar = intersectingWordOfInterest.getIntersection(gridWord).getContents().charAt(0);
					Pair<Character, GridWord> previouslyCheckedPair = previouslyChecked.get(previouslyCheckedIndex);

//System.out.println("previouslyCheckedPair.first_ = " + previouslyCheckedPair.first_ + ", intersectChar = " + intersectChar + ", previouslyCheckedPair.second_ = " + previouslyCheckedPair.second_ + ", intersectingWordOfInterest = " + intersectingWordOfInterest);
					if (previouslyCheckedPair.first == intersectChar && previouslyCheckedPair.second == intersectingWordOfInterest)
					{
						System.out.println("Found match! unboxing worked!");
						skipChildCheck = true;
					} else {
						// We have divergence from previous. Don't pay further attention to previouslyChecked
						examinePreviouslyChecked = false;
					}
				}

//System.out.println("skipChildCheck = " + skipChildCheck);
				if (! skipChildCheck) {
					char intersectChar = intersectingWordOfInterest.getIntersection(gridWord).getContents().charAt(0);
					if (! populateWord(intersectingWordOfInterest, wordToCrossingWordsMap, originalValueStore, dict, smallestListOfMatches)) {
						// Cannot satisfy this intersecting word
						continue AcrossWord;
					}
					//TODO record here that we successfully checked intersectingWordOfInterest in order, with letter L
					Pair<Character, GridWord> newPair = new Pair<Character, GridWord>(intersectChar, intersectingWordOfInterest);
System.out.println("Adding word to previously checked " + intersectChar + ", index = " + previouslyCheckedIndex + ", size = " + previouslyChecked.size());					
					if (previouslyCheckedIndex == previouslyChecked.size())
						previouslyChecked.add(newPair);
					else
						previouslyChecked.set(previouslyCheckedIndex, newPair);
				}
				
				
				alreadyChecked[smallestListIndex] = true;
				--numToCheck;
				++previouslyCheckedIndex;
//				intersectingWordsOfInterest.remove(intersectingWordOfInterest);
//				intersectingWordsToCheck.remove(smallestListIndex);
			}
		}
		gridWord.restoreFromFillConfig(fillConfig);
		return false;
	}
	

}
