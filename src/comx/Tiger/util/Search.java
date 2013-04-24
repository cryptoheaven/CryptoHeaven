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
// Source File Name:   Search.java

package comx.Tiger.util;

import java.util.Vector;

// Referenced classes of package com.wintertree.util:
//      Comparable

public class Search {

  public Search() {
  }

  public static int binary(Comparable acomparable[], Comparable comparable) {
    int i = 0;
    for (int j = acomparable.length - 1; i <= j;) {
      int k = (i + j) / 2;
      int l = comparable.compareTo(acomparable[k]);
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return k;
    }

    return -1;
  }

  public static int binary(Vector vector, Comparable comparable) {
    int i = 0;
    for (int j = vector.size() - 1; i <= j;) {
      int k = (i + j) / 2;
      Comparable comparable1 = (Comparable)vector.elementAt(k);
      int l = comparable.compareTo(comparable1);
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return k;
    }

    return -1;
  }

  public static int binary(Vector vector, String s) {
    int i = 0;
    for (int j = vector.size() - 1; i <= j;) {
      int k = (i + j) / 2;
      String s1 = (String)vector.elementAt(k);
      int l = s.compareTo(s1);
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return k;
    }

    return -1;
  }

  public static int binary(String as[], String s) {
    int i = 0;
    for (int j = as.length - 1; i <= j;) {
      int k = (i + j) / 2;
      String s1 = as[k];
      int l = s.compareTo(s1);
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return k;
    }

    return -1;
  }

  public static int binary(char ac[], char c) {
    int i = 0;
    for (int j = ac.length - 1; i <= j;) {
      int k = (i + j) / 2;
      int l = c - ac[k];
      if (l < 0)
        j = k - 1;
      else
        if (l > 0)
          i = k + 1;
        else
          return k;
    }

    return -1;
  }
}