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
// Source File Name:   MemTextLexicon.java

package comx.Tiger.ssce;

import java.util.Enumeration;
import java.util.Hashtable;

// Referenced classes of package com.wintertree.ssce:
//      LexiconUpdateException, ParameterException, WordException, EditableLexicon,
//      SuggestionSet, WordComparator

public class MemTextLexicon implements EditableLexicon {

  protected int language;
  Hashtable theWords;

  public MemTextLexicon() {
    this(30840);
  }

  public MemTextLexicon(int i) {
    language = i;
    theWords = new Hashtable();
  }

  public void addWord(String s) throws LexiconUpdateException {
    try {
      addWord(s, 105, "");
    }
    catch (Exception exception) {
      throw new LexiconUpdateException(s);
    }
  }

  public void addWord(String s, int i) throws ParameterException, LexiconUpdateException {
    if (i != 101 && i != 105) {
      throw new ParameterException(i + " action requires other word");
    } else {
      addWord(s, i, "");
      return;
    }
  }

  public void addWord(String s, int i, String s1) throws ParameterException, LexiconUpdateException {
    if (i != 97 && i != 65 && i != 99 && i != 67 && i != 101 && i != 105) {
      throw new ParameterException("Action unrecognized: " + i);
    } else {
      theWords.put(s, (char)i + s1);
      return;
    }
  }

  public void clear() {
    theWords.clear();
  }

  public boolean equals(Object obj) {
    if (obj instanceof MemTextLexicon) {
      MemTextLexicon memtextlexicon = (MemTextLexicon)obj;
      if (theWords.size() != memtextlexicon.theWords.size())
        return false;
      for (Enumeration enumeration = theWords.keys(); enumeration.hasMoreElements();) {
        String s = (String)enumeration.nextElement();
        String s1 = (String)theWords.get(s);
        String s2 = (String)memtextlexicon.theWords.get(s);
        if (s1 == null && s2 != null || s1 != null && s2 == null)
          return false;
        if (s1 != null && !s1.equals(s2))
          return false;
      }
      return true;
    } else {
      return false;
    }
  }

  public int hashCode() {
    assert false : "hashCode not designed";
    return 0;
  }

  public int findWord(String s, boolean flag, StringBuffer stringbuffer) {
    label0: {
      if (flag) {
        if (theWords.containsKey(s)) {
          String s1 = (String)theWords.get(s);
          stringbuffer.setLength(0);
          stringbuffer.append(s1.substring(1));
          return s1.charAt(0);
        }
        break label0;
      }
      Enumeration enumeration = theWords.keys();
      String s2;
      do {
        if (!enumeration.hasMoreElements())
          break label0;
        s2 = (String)enumeration.nextElement();
      } while (!s2.equalsIgnoreCase(s));
      String s3 = (String)theWords.get(s2);
      stringbuffer.setLength(0);
      stringbuffer.append(s3.substring(1));
      return s3.charAt(0);
    }
    return 0;
  }

  public void deleteWord(String s) throws WordException, LexiconUpdateException {
    StringBuffer stringbuffer = new StringBuffer();
    if (findWord(s, true, stringbuffer) == 0) {
      throw new WordException(s + " not found");
    } else {
      theWords.remove(s);
      return;
    }
  }

  public int size() {
    return theWords.size();
  }

  public void suggest(String s, int i, WordComparator wordcomparator, SuggestionSet suggestionset) {
    Enumeration enumeration = words();
    do {
      if (!enumeration.hasMoreElements())
        break;
      String s1 = (String)enumeration.nextElement();
      String s2 = (String)theWords.get(s1);
      char c = s2.charAt(0);
      switch (c) {
        case 97: // 'a'
        case 99: // 'c'
          if (s.equals(s1)) {
            WordComparator _tmp = wordcomparator;
            suggestionset.add(s2.substring(1), 100 - 1, false);
          }
          break;

        case 65: // 'A'
        case 67: // 'C'
          if (s.equals(s1)) {
            WordComparator _tmp1 = wordcomparator;
            suggestionset.add(s2.substring(1), 100 - 1, true);
          }
          break;

        case 101: // 'e'
          int j = 0;
          while (j < suggestionset.size()) {
            if (suggestionset.wordAt(j).equals(s1))
              suggestionset.deleteAt(j);
            j++;
          }
          break;

        case 105: // 'i'
          int k = wordcomparator.compare(s, s1);
          suggestionset.add(s1, k, true);
          break;
      }
    } while (true);
  }

  public int getLanguage() {
    return language;
  }

  public String toString() {
    StringBuffer stringbuffer = new StringBuffer(getClass().getName() + '(');
    int i = 0;
    for (Enumeration enumeration = theWords.keys(); i < 3 && enumeration.hasMoreElements(); i++) {
      stringbuffer.append((String)enumeration.nextElement());
      stringbuffer.append(", ");
    }

    stringbuffer.append("...)");
    return stringbuffer.toString();
  }

  public Enumeration words() {
    return theWords.keys();
  }
}