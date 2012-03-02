/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
// Source File Name:   TypographicalComparator.java

package comx.Tiger.ssce;

import comx.Tiger.util.UniCharacter;

// Referenced classes of package com.wintertree.ssce:
//      WordComparator

public class TypographicalComparator implements WordComparator {

  private boolean isAccented;
  static final int CASE_MISMATCH_PENALTY = 5;
  static final int ACCENT_MISMATCH_PENALTY = 10;
  static final int DOUBLE_CONSONANT_INS_DEL_PENALTY = 20;
  static final int DOUBLE_VOWEL_INS_DEL_PENALTY = 25;
  static final int VOWEL_TRANSPOSITION_PENALTY = 20;
  static final int CONSONANT_TRANSPOSITION_PENALTY = 25;
  static final int TRANSPOSITION_PENALTY = 40;
  static final int INS_DEL_PENALTY = 50;
  static final int SUBSTITUTE_PENALTY = 100;
  static final int EXACT_MATCH_SCORE = 100;
  static final int FIRST_BASE = 65;
  private static final short charSimTbl[][] = {
    {
      100, 0, 5, 15, 40, 0, 0, 0, 20, 0,
      0, 0, 0, 0, 40, 0, 40, 0, 20, 0,
      20, 0, 20, 0, 0, 0
    }, {
      0, 100, 0, 15, 0, 5, 10, 15, 0, 0,
      0, 5, 0, 20, 10, 20, 5, 0, 0, 10,
      0, 20, 0, 0, 0, 0
    }, {
      5, 0, 100, 20, 20, 10, 0, 0, 0, 0,
      20, 0, 0, 0, 10, 0, 20, 0, 10, 0,
      0, 20, 0, 20, 0, 20
    }, {
      15, 15, 20, 100, 10, 20, 0, 0, 0, 0,
      0, 5, 0, 0, 5, 5, 5, 5, 20, 20,
      0, 0, 5, 10, 0, 0
    }, {
      40, 0, 20, 10, 100, 0, 0, 0, 20, 0,
      0, 0, 0, 0, 40, 0, 0, 20, 10, 5,
      20, 0, 20, 0, 5, 0
    }, {
      0, 5, 10, 20, 0, 100, 20, 0, 5, 0,
      0, 0, 0, 0, 0, 5, 0, 10, 10, 10,
      0, 30, 0, 0, 0, 0
    }, {
      0, 10, 0, 0, 0, 20, 100, 20, 0, 20,
      0, 0, 0, 0, 0, 0, 5, 5, 0, 10,
      0, 10, 0, 0, 5, 0
    }, {
      0, 15, 0, 0, 0, 0, 20, 100, 0, 20,
      20, 10, 0, 10, 0, 0, 0, 0, 0, 0,
      5, 0, 0, 0, 10, 0
    }, {
      20, 0, 0, 0, 20, 5, 0, 0, 100, 25,
      10, 25, 0, 0, 40, 0, 0, 0, 0, 5,
      40, 0, 0, 0, 0, 0
    }, {
      0, 0, 0, 0, 0, 0, 20, 20, 25, 100,
      20, 0, 10, 10, 0, 0, 0, 0, 0, 0,
      10, 0, 0, 0, 0, 0
    }, {
      0, 0, 20, 0, 0, 0, 0, 20, 10, 20,
      100, 30, 10, 0, 5, 0, 20, 0, 0, 0,
      0, 0, 0, 0, 0, 0
    }, {
      0, 5, 0, 5, 0, 0, 0, 10, 25, 0,
      30, 100, 0, 0, 5, 0, 0, 5, 0, 20,
      0, 0, 0, 0, 0, 0
    }, {
      0, 0, 0, 0, 0, 0, 0, 0, 0, 10,
      10, 0, 100, 60, 0, 0, 0, 0, 0, 0,
      0, 0, 10, 0, 0, 0
    }, {
      0, 20, 0, 0, 0, 0, 0, 10, 0, 10,
      0, 0, 60, 100, 0, 0, 0, 0, 0, 0,
      20, 0, 0, 0, 0, 0
    }, {
      40, 10, 10, 5, 40, 0, 0, 0, 40, 0,
      5, 0, 0, 0, 100, 30, 20, 0, 0, 0,
      20, 0, 10, 0, 0, 0
    }, {
      0, 20, 0, 5, 0, 5, 0, 0, 0, 0,
      0, 0, 0, 0, 30, 100, 20, 0, 0, 20,
      0, 0, 0, 0, 0, 0
    }, {
      40, 5, 20, 5, 0, 0, 5, 0, 0, 0,
      20, 0, 0, 0, 20, 20, 100, 0, 0, 0,
      0, 0, 20, 0, 0, 0
    }, {
      0, 0, 0, 5, 20, 10, 5, 0, 0, 0,
      0, 5, 0, 0, 0, 0, 0, 100, 0, 20,
      0, 0, 0, 0, 0, 0
    }, {
      20, 0, 10, 20, 10, 10, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 100, 0,
      0, 0, 10, 20, 0, 25
    }, {
      0, 10, 20, 5, 10, 10, 0, 0, 5, 0,
      0, 20, 0, 0, 0, 20, 0, 20, 0, 100,
      0, 0, 0, 0, 20, 0
    }, {
      20, 0, 0, 0, 20, 0, 0, 5, 40, 10,
      0, 0, 0, 20, 20, 0, 0, 0, 0, 0,
      100, 20, 10, 0, 20, 0
    }, {
      0, 20, 20, 0, 0, 30, 10, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      20, 100, 20, 0, 10, 0
    }, {
      20, 0, 0, 5, 20, 0, 0, 0, 0, 0,
      0, 0, 10, 0, 10, 0, 20, 0, 10, 0,
      10, 20, 100, 0, 0, 0
    }, {
      0, 0, 20, 10, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 20, 0,
      0, 0, 0, 100, 0, 20
    }, {
      0, 0, 0, 0, 5, 0, 5, 10, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 20,
      20, 10, 0, 0, 100, 0
    }, {
      0, 0, 20, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 25, 0,
      0, 0, 0, 20, 0, 100
    }
  };

  public TypographicalComparator() {
    isAccented = false;
  }

  public TypographicalComparator(boolean flag) {
    isAccented = flag;
  }

  public int compare(String s, String s1) {
    int i = 0;
    int j = 0;
    int k = 0;
    int l;
    for (l = 0; k < s.length() && l < s1.length();) {
      j++;
      char c = s.charAt(k);
      char c1 = s1.charAt(l);
      if (c != c1) {
        char c4 = '\0';
        char c5 = '\0';
        char c2;
        char c3;
        if (isAccented || !UniCharacter.isASCII(c) || !UniCharacter.isASCII(c1)) {
          c2 = UniCharacter.toBase(c);
          c3 = UniCharacter.toBase(c1);
        } else {
          c2 = Character.toUpperCase(c);
          c3 = Character.toUpperCase(c1);
        }
        if (c2 == c3) {
          if (UniCharacter.isLowerCase(c) != UniCharacter.isLowerCase(c1))
            i += 95;
          if (Character.toLowerCase(c) != Character.toLowerCase(c1))
            i += 90;
          k++;
          l++;
        } else {
          if (isAccented) {
            if (k + 1 < s.length())
              c4 = UniCharacter.toBase(s.charAt(k + 1));
            if (l + 1 < s1.length())
              c5 = UniCharacter.toBase(s1.charAt(l + 1));
          } else {
            if (k + 1 < s.length()) {
              char c6 = s.charAt(k + 1);
              if (UniCharacter.isASCII(c6))
                c4 = Character.toUpperCase(c6);
              else
                c4 = UniCharacter.toBase(c6);
            }
            if (l + 1 < s1.length()) {
              char c7 = s1.charAt(l + 1);
              if (UniCharacter.isASCII(c7))
                c5 = Character.toUpperCase(s1.charAt(l + 1));
              else
                c5 = UniCharacter.toBase(c7);
            }
          }
          if (c4 != 0 && c5 != 0 && c3 == c4 && c5 == c2) {
            if (UniCharacter.isVowel(c2) && UniCharacter.isVowel(c3))
              i += 160;
            else
              if (!UniCharacter.isVowel(c2) && !UniCharacter.isVowel(c3))
                i += 150;
              else
                i += 120;
            k += 2;
            l += 2;
            j++;
          } else
            if (c4 != 0 && c3 == c4) {
              if (k > 0 && l > 0 && c == s.charAt(k - 1) && c == s1.charAt(l - 1)) {
                if (UniCharacter.isVowel(c))
                  i += 75;
                else
                  i += 80;
              } else {
                i += 50;
              }
              k++;
            } else
              if (c5 != 0 && c2 == c5) {
                if (k > 0 && l > 0 && c1 == s1.charAt(l - 1) && c1 == s.charAt(k - 1)) {
                  if (UniCharacter.isVowel(c1))
                    i += 75;
                  else
                    i += 80;
                } else {
                  i += 50;
                }
                l++;
              } else
                if (c2 >= 'A' && c2 - 65 < charSimTbl.length && c3 >= 'A' && c3 - 65 < charSimTbl[0].length) {
                  i += charSimTbl[c2 - 65][c3 - 65];
                  k++;
                  l++;
                } else {
                  i += 0;
                  k++;
                  l++;
                }
        }
      } else {
        i += 100;
        k++;
        l++;
      }
    }

    if (i <= 0)
      return 0;
    while (k++ < s.length()) {
      i += 50;
      j++;
    }
    while (l++ < s1.length()) {
      i += 50;
      j++;
    }
    if (i <= 0 || j <= 0)
      return 0;
    else
      return i / j;
  }

}