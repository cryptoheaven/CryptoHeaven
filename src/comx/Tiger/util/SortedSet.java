/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
// Source File Name:   SortedSet.java

package comx.Tiger.util;

import java.util.Enumeration;
import java.util.Vector;

// Referenced classes of package com.wintertree.util:
//      Comparable, Search

public class SortedSet {

  protected Vector set;

  public SortedSet() {
    set = new Vector();
  }

  public void addElement(Comparable comparable) {
    if (Search.binary(set, comparable) >= 0)
      return;
    int i = 0;
    do {
      if (i >= set.size())
        break;
      Comparable comparable1 = (Comparable)set.elementAt(i);
      if (comparable.compareTo(comparable1) <= 0)
        break;
      i++;
    } while (true);
    set.insertElementAt(comparable, i);
  }

  public void addElement(String s) {
    if (Search.binary(set, s) >= 0)
      return;
    int i = 0;
    do {
      if (i >= set.size())
        break;
      String s1 = (String)set.elementAt(i);
      if (s.compareTo(s1) <= 0)
        break;
      i++;
    } while (true);
    set.insertElementAt(s, i);
  }

  public Enumeration elements() {
    return set.elements();
  }

  public int indexOf(String s) {
    return Search.binary(set, s);
  }

  public int indexOf(Comparable comparable) {
    return Search.binary(set, comparable);
  }

  public void removeAllElements() {
    set.removeAllElements();
  }

  public boolean removeElement(Comparable comparable) {
    int i = Search.binary(set, comparable);
    if (i >= 0) {
      set.removeElementAt(i);
      return true;
    } else {
      return false;
    }
  }

  public int size() {
    return set.size();
  }
}