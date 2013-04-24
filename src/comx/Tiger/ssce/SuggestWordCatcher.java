/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
// Source File Name:   CompressedLexicon.java

package comx.Tiger.ssce;


// Referenced classes of package com.wintertree.ssce:
//      WordCatcher, WordComparator, SuggestionSet

class SuggestWordCatcher implements WordCatcher {

  protected WordComparator comparator;
  protected String keyWord;
  protected int nSuggestions;
  protected SuggestionSet suggestions;

  public SuggestWordCatcher(String s, WordComparator wordcomparator, SuggestionSet suggestionset) {
    keyWord = s;
    comparator = wordcomparator;
    suggestions = suggestionset;
    nSuggestions = 0;
  }

  public boolean catchWord(String s) {
    int i = comparator.compare(keyWord, s);
    if (suggestions.add(s, i))
      nSuggestions++;
    return true;
  }
}