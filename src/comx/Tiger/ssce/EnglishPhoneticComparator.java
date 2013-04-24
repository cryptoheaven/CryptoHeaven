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
// Source File Name:   EnglishPhoneticComparator.java

package comx.Tiger.ssce;

import comx.Tiger.util.UniCharacter;

// Referenced classes of package com.wintertree.ssce:
//      TypographicalComparator, WordComparator

public class EnglishPhoneticComparator implements WordComparator {

  public EnglishPhoneticComparator() {
  }

  public int compare(String s, String s1) {
    String s2 = phoneticSig(s.toLowerCase());
    String s3 = phoneticSig(s1.toLowerCase());
    if (s2.startsWith(s3) || s3.startsWith(s2)) {
      TypographicalComparator typographicalcomparator = new TypographicalComparator();
      return typographicalcomparator.compare(s, s1);
    } else {
      return 0;
    }
  }

  protected String phoneticSig(String s) {
    StringBuffer stringbuffer = new StringBuffer();
    int i = 0;
    switch (s.charAt(i)) {
      case 103: // 'g'
      case 107: // 'k'
      case 112: // 'p'
        if (i + 1 < s.length() && 'n' == s.charAt(i + 1)) {
          stringbuffer.append('N');
          i += 2;
        }
        break;

      case 97: // 'a'
        if (i + 1 < s.length() && 'e' == s.charAt(i + 1)) {
          stringbuffer.append('E');
          i += 2;
        } else {
          stringbuffer.append('A');
          i++;
        }
        break;

      case 101: // 'e'
      case 105: // 'i'
      case 111: // 'o'
      case 117: // 'u'
        stringbuffer.append(Character.toUpperCase(s.charAt(i)));
        i++;
        break;

      case 119: // 'w'
        if (i + 1 < s.length() && 'r' == s.charAt(i + 1)) {
          stringbuffer.append('R');
          i += 2;
        } else
          if (i + 1 < s.length() && 'h' == s.charAt(i + 1)) {
            stringbuffer.append('H');
            i += 2;
          }
        break;

      case 120: // 'x'
        stringbuffer.append('S');
        i++;
        break;
    }
    do {
      if (i >= s.length())
        break;
      char c = s.charAt(i);
      if (i + 1 < s.length() && c == s.charAt(i + 1))
        if ('c' == c) {
          stringbuffer.append("KK");
          i += 2;
        } else
          if ('g' == c) {
            stringbuffer.append('K');
            i += 2;
          } else {
            i++;
          }
      switch (c) {
        case 98: // 'b'
          stringbuffer.append('B');
          i++;
          break;

        case 99: // 'c'
          if (i + 2 < s.length() && 'i' == s.charAt(i + 1) && 'a' == s.charAt(i + 2)) {
            stringbuffer.append('X');
            i += 3;
          } else
            if (i + 1 < s.length() && ('i' == s.charAt(i + 1) || 'e' == s.charAt(i + 1) || 'y' == s.charAt(i + 1))) {
              stringbuffer.append('S');
              i += 2;
            } else
              if (i + 1 < s.length() && 'k' == s.charAt(i + 1)) {
                stringbuffer.append('K');
                i += 2;
              } else {
                stringbuffer.append('K');
                i++;
              }
          break;

        case 100: // 'd'
          if (i + 2 < s.length() && 'g' == s.charAt(i + 1) && ('e' == s.charAt(i + 2) || 'y' == s.charAt(i + 2) || 'i' == s.charAt(i + 2))) {
            stringbuffer.append('J');
            i += 3;
          } else {
            stringbuffer.append('T');
            i++;
          }
          break;

        case 102: // 'f'
          stringbuffer.append('F');
          i++;
          break;

        case 103: // 'g'
          if (i + 1 < s.length() && 'h' == s.charAt(i + 1) && (i + 2 <= s.length() || UniCharacter.isVowel(s.charAt(i + 2)))) {
            stringbuffer.append('K');
            i += 2;
          } else
            if (i + 1 < s.length() && 'n' == s.charAt(i + 1)) {
              stringbuffer.append('N');
              i += 2;
            } else
              if (i + 1 < s.length() && 'g' == s.charAt(i + 1)) {
                stringbuffer.append('K');
                i += 2;
              } else
                if (i + 1 < s.length() && ('i' == s.charAt(i + 1) || 'e' == s.charAt(i + 1) || 'y' == s.charAt(i + 1))) {
                  stringbuffer.append('J');
                  i += 2;
                } else {
                  stringbuffer.append('K');
                  i++;
                }
          break;

        case 104: // 'h'
          if (i > 0 && UniCharacter.isVowel(s.charAt(i - 1)) && i + 1 < s.length() && !UniCharacter.isVowel(s.charAt(i + 1))) {
            i++;
          } else {
            stringbuffer.append('H');
            i++;
          }
          break;

        case 106: // 'j'
          stringbuffer.append('J');
          i++;
          break;

        case 107: // 'k'
          stringbuffer.append('K');
          i++;
          break;

        case 108: // 'l'
          stringbuffer.append('L');
          i++;
          break;

        case 109: // 'm'
          if (i + 2 == s.length() && 'b' == s.charAt(i + 1)) {
            stringbuffer.append('M');
            i += 2;
          } else {
            stringbuffer.append('M');
            i++;
          }
          break;

        case 110: // 'n'
          stringbuffer.append('N');
          i++;
          break;

        case 112: // 'p'
          if (i + 1 < s.length() && 'h' == s.charAt(i + 1)) {
            stringbuffer.append('F');
            i += 2;
          } else {
            stringbuffer.append('P');
            i++;
          }
          break;

        case 113: // 'q'
          stringbuffer.append('K');
          i++;
          break;

        case 114: // 'r'
          stringbuffer.append('R');
          i++;
          break;

        case 115: // 's'
          if (i + 1 < s.length() && 'h' == s.charAt(i + 1)) {
            stringbuffer.append('X');
            i += 2;
          } else
            if (i + 2 < s.length() && 'i' == s.charAt(i + 1) && ('o' == s.charAt(i + 2) || 'a' == s.charAt(i + 2))) {
              stringbuffer.append('X');
              i += 3;
            } else
              if (i + 2 < s.length() && 'c' == s.charAt(i + 1) && 'h' == s.charAt(i + 2)) {
                stringbuffer.append("SK");
                i += 3;
              } else {
                stringbuffer.append('S');
                i++;
              }
          break;

        case 116: // 't'
          if (i + 1 < s.length() && 'h' == s.charAt(i + 1)) {
            stringbuffer.append('0');
            i += 2;
          } else
            if (i + 2 < s.length() && 'i' == s.charAt(i + 1) && ('o' == s.charAt(i + 2) || 'a' == s.charAt(i + 2))) {
              stringbuffer.append('X');
              i += 3;
            } else
              if (i + 2 < s.length() && 'c' == s.charAt(i + 1) && 'h' == s.charAt(i + 2)) {
                stringbuffer.append('X');
                i += 3;
              } else {
                stringbuffer.append('T');
                i++;
              }
          break;

        case 118: // 'v'
          stringbuffer.append('F');
          i++;
          break;

        case 119: // 'w'
          if (i + 1 < s.length() && UniCharacter.isVowel(s.charAt(i + 1))) {
            stringbuffer.append('W');
            i++;
          } else {
            i++;
          }
          break;

        case 120: // 'x'
          stringbuffer.append("KS");
          i++;
          break;

        case 121: // 'y'
          if (i + 1 < s.length() && UniCharacter.isVowel(s.charAt(i + 1))) {
            stringbuffer.append('Y');
            i++;
          } else {
            i++;
          }
          break;

        case 122: // 'z'
          stringbuffer.append('S');
          i++;
          break;

        case 101: // 'e'
        case 105: // 'i'
        case 111: // 'o'
        case 117: // 'u'
        default:
          i++;
          break;
      }
    } while (true);
    return stringbuffer.toString();
  }
}