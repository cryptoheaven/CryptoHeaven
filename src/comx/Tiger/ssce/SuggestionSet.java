/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
// Source File Name:   SuggestionSet.java

package comx.Tiger.ssce;

import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.wintertree.ssce:
//      Suggestion

public class SuggestionSet {

  private int maxWords;
  private Vector suggestions;

  public SuggestionSet(int i) {
    maxWords = i;
    if (maxWords <= 0)
      maxWords = 1;
    suggestions = new Vector();
  }

  public boolean add(String s, int i, boolean flag) {
    if (i <= 0)
      return false;
    if (suggestions.size() == maxWords) {
      Suggestion suggestion = (Suggestion)suggestions.elementAt(suggestions.size() - 1);
      if (i <= suggestion.score)
        return false;
    }
    Suggestion suggestion1 = new Suggestion();
    suggestion1.word = s;
    suggestion1.score = i;
    suggestion1.matchCase = flag;
    int j = suggestions.indexOf(suggestion1);
    if (j >= 0)
      return false;
    int k = 0;
    do {
      if (k >= suggestions.size())
        break;
      Suggestion suggestion2 = (Suggestion)suggestions.elementAt(k);
      if (i > suggestion2.score)
        break;
      k++;
    } while (true);
    if (suggestions.size() >= maxWords)
      suggestions.setSize(suggestions.size() - 1);
    suggestions.insertElementAt(suggestion1, k);
    return true;
  }

  public boolean add(String s, int i) {
    return add(s, i, true);
  }

  public void clear() {
    suggestions.removeAllElements();
  }

  public void deleteAt(int i) {
    suggestions.removeElementAt(i);
  }

  public boolean getMatchCaseAt(int i) {
    Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
    return suggestion.matchCase;
  }

  public void replaceAt(int i, String s) {
    Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
    suggestion.word = s;
    suggestions.setElementAt(suggestion, i);
  }

  public int size() {
    return suggestions.size();
  }

  public int scoreAt(int i) throws ArrayIndexOutOfBoundsException {
    Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
    return suggestion.score;
  }

  public String toString() {
    StringBuffer stringbuffer = new StringBuffer();
    for (int i = 0; i < suggestions.size(); i++) {
      Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
      stringbuffer.append(suggestion.word);
      if (i < suggestions.size() - 1)
        stringbuffer.append(", ");
    }

    return stringbuffer.toString();
  }

  public String wordAt(int i) throws ArrayIndexOutOfBoundsException {
    Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
    return suggestion.word;
  }

  public Enumeration words() {
    Vector vector = new Vector(suggestions.size());
    for (int i = 0; i < suggestions.size(); i++) {
      Suggestion suggestion = (Suggestion)suggestions.elementAt(i);
      vector.addElement(suggestion.word);
    }

    return vector.elements();
  }
}