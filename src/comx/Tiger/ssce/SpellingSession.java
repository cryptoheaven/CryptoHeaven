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
// Source File Name:   SpellingSession.java

package comx.Tiger.ssce;

import comx.Tiger.util.UniCharacter;

import java.io.Serializable;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      UnsupportedException, Lexicon, WordParser, WordComparator,
//      SuggestionSet

public class SpellingSession implements Serializable, Cloneable {

  public static final int MAX_SUGGEST_DEPTH = 100;
  public static final int MAX_WORD_LEN = 63;
  public static final int CASE_SENSITIVE_OPT = 1;
  public static final int IGNORE_ALL_CAPS_WORD_OPT = 2;
  public static final int IGNORE_CAPPED_WORD_OPT = 4;
  public static final int IGNORE_MIXED_CASE_OPT = 8;
  public static final int IGNORE_MIXED_DIGITS_OPT = 16;
  public static final int IGNORE_NON_ALPHA_WORD_OPT = 32;
  public static final int REPORT_DOUBLED_WORD_OPT = 64;
  public static final int REPORT_MIXED_CASE_OPT = 128;
  public static final int REPORT_MIXED_DIGITS_OPT = 256;
  public static final int REPORT_SPELLING_OPT = 512;
  public static final int REPORT_UNCAPPED_OPT = 1024;
  public static final int SPLIT_CONTRACTED_WORDS_OPT = 2048;
  public static final int SPLIT_HYPHENATED_WORDS_OPT = 4096;
  public static final int SPLIT_WORDS_OPT = 8192;
  public static final int STRIP_POSSESSIVES_OPT = 16384;
  public static final int SUGGEST_SPLIT_WORDS_OPT = 32768;
  public static final int IGNORE_DOMAIN_NAMES_OPT = 0x10000;
  public static final int ALLOW_ACCENTED_CAPS_OPT = 0x20000;

  public static final int OK_RSLT = 0;
  public static final int AUTO_CHANGE_WORD_RSLT = 1;
  public static final int CONDITIONALLY_CHANGE_WORD_RSLT = 2;
  public static final int DOUBLED_WORD_RSLT = 4;
  public static final int END_OF_TEXT_RSLT = 8;
  public static final int MISSPELLED_WORD_RSLT = 16;
  public static final int MIXED_CASE_WORD_RSLT = 32;
  public static final int MIXED_DIGITS_WORD_RSLT = 64;
  public static final int UNCAPPED_WORD_RSLT = 128;
  private static final int EXCLUDE_WORD_RSLT = 4096;

  private Lexicon lexicons[];
  private int options;
  private static Hashtable opts = null;
  private String misspelledWord;
  private int misspelledWordOffset;

  public SpellingSession() {
    lexicons = null;
    options = 0x25621;
    misspelledWord = null;
    misspelledWordOffset = 0;
  }

  public int check(String s) {
    StringBuffer stringbuffer = new StringBuffer(s.length());
    return check(s, stringbuffer);
  }

  public int check(String s, StringBuffer stringbuffer) {
    int i = 0;
    String s1 = s;
    if ((options & 0x4000) != 0)
      s1 = stripPossessives(s);
    int j = s1.length();
    if (j == 0)
      return i;
    stringbuffer.setLength(0);
    stringbuffer.append(s);
    misspelledWord = s;
    misspelledWordOffset = 0;
    if ((options & 0x200) == 0 && ignoreWord(s1))
      return i;
    if ((options & 0x200) != 0) {
      int k = checkWord2(s1, stringbuffer);
      if ((k & 0x10) != 0) {
        if ((options & 0x2000) != 0) {
          boolean flag1 = false;
          for (int j2 = 2; j2 <= j - 2 && !flag1; j2++) {
            String s3 = s1.substring(0, j2);
            String s4 = misspelledWord;
            int i3 = misspelledWordOffset;
            options &= 0xffffdfff;
            boolean flag3 = check(s3) == 0;
            options |= 0x2000;
            if (flag3 && check(s1.substring(j2)) == 0)
              flag1 = true;
            misspelledWord = s4;
            misspelledWordOffset = i3;
          }

          if (flag1)
            k &= 0xffffffef;
        }
        if ((k & 0x10) != 0 && (options & 0x1800) != 0 && containsSubWords(s1)) {
          boolean flag2 = true;
          int k2 = 0;
          StringBuffer stringbuffer2 = new StringBuffer();
          do {
            if (k2 >= j || !flag2)
              break;
            misspelledWordOffset = k2;
            k2 += getSubWord(s1.substring(k2), stringbuffer2);
            int l2 = checkWord2(stringbuffer2.toString(), stringbuffer);
            if ((l2 & 0x10) != 0) {
              misspelledWord = stringbuffer2.toString();
              flag2 = false;
            }
          } while (true);
          if (flag2)
            k &= 0xffffffef;
        }
      }
      i |= k;
    }
    if ((options & 0x80) != 0 && isMixedCase(s1)) {
      i |= 0x20;
      for (int l = 0; l < stringbuffer.length(); l++)
        stringbuffer.setCharAt(l, Character.toUpperCase(stringbuffer.charAt(l)));

      if (UniCharacter.isUpperCase(s.charAt(0)) && stringbuffer.length() > 0)
        stringbuffer.setCharAt(0, Character.toUpperCase(stringbuffer.charAt(0)));
    }
    if ((options & 0x100) != 0) {
      int i1 = 0;
      do {
        if (i1 >= j)
          break;
        char c = s1.charAt(i1);
        if (!UniCharacter.isApostrophe(c) && !UniCharacter.isLetter(c) && !UniCharacter.isHyphen(c)) {
          i |= 0x40;
          break;
        }
        i1++;
      } while (true);
    }
    if ((i & 0x40) != 0) {
      boolean flag = false;
      if (stringbuffer.length() > 0)
        flag = UniCharacter.isLetter(stringbuffer.charAt(0));
      for (int i2 = 0; i2 < stringbuffer.length(); i2++) {
        char c2 = stringbuffer.charAt(i2);
        if (flag && UniCharacter.isDigit(c2) || !flag && UniCharacter.isLetter(c2)) {
          stringbuffer.insert(i2, ' ');
          i2++;
        }
        flag = UniCharacter.isLetter(c2);
      }

    }
    if ((options & 0x400) != 0 && (options & 1) != 0 && (i & 0x10) != 0 && UniCharacter.isLowerCase(s.charAt(0))) {
      StringBuffer stringbuffer1 = new StringBuffer();
      stringbuffer1.append(Character.toUpperCase(s1.charAt(0)));
      stringbuffer1.append(s.substring(1));
      int j1 = checkWord3(stringbuffer1.toString(), s1, stringbuffer);
      if (j1 != 0) {
        stringbuffer.setLength(0);
        stringbuffer.append(stringbuffer1.toString());
        i |= 0x80;
      } else {
        String s2 = s.toUpperCase();
        int k1 = checkWord3(s2, s1, stringbuffer);
        if (k1 != 0) {
          stringbuffer.setLength(0);
          stringbuffer.append(s2);
          i |= 0x80;
        }
      }
    }
    if ((options & 0x20000) == 0 && (i & 0x10) == 0) {
      int l1 = 0;
      do {
        if (l1 >= j)
          break;
        char c1 = s1.charAt(l1);
        if (UniCharacter.isUpperCase(c1) && c1 != UniCharacter.toBase(c1)) {
          i |= 0x10;
          matchCase(stringbuffer, false, true, stringbuffer.toString());
          break;
        }
        l1++;
      } while (true);
    }
    if (stringbuffer.length() > 0)
      restorePossessives(stringbuffer, s);
    return i;
  }

  public int check(WordParser wordparser, StringBuffer stringbuffer) {
    boolean flag = false;
    for (; wordparser.hasMoreElements(); wordparser.nextWord()) {
      String s = wordparser.getWord();
      int i = check(s, stringbuffer);
      if ((options & 0x40) != 0 && !ignoreWord(s) && wordparser.isDoubledWord((options & 1) != 0)) {
        i |= 4;
        misspelledWordOffset = 0;
        misspelledWord = s;
      }
      if (i != 0) {
        if ((i & 0x10) != 0) {
          wordparser.setCursor(wordparser.getCursor() + misspelledWordOffset);
          wordparser.setWordLength(misspelledWord.length());
        }
        return i;
      }
    }

    return 8;
  }

  public Object clone() {
    SpellingSession spellingsession = null;
    try {
      spellingsession = (SpellingSession)super.clone();
    }
    catch (CloneNotSupportedException clonenotsupportedexception) {
      throw new InternalError();
    }
    spellingsession.setLexicons(getLexicons());
    spellingsession.options = options;
    if (misspelledWord != null)
      spellingsession.misspelledWord = misspelledWord;
    else
      spellingsession.misspelledWord = null;
    spellingsession.misspelledWordOffset = misspelledWordOffset;
    return spellingsession;
  }

  public Lexicon[] getLexicons() {
    return lexicons;
  }

  public boolean getOption(int i) {
    return (options & i) != 0;
  }

  public static int getOption(Integer integer) {
    if (opts != null) {
      Integer integer1 = (Integer)opts.get(integer);
      if (integer1 == null)
        return 0;
      else
        return integer1.intValue();
    } else {
      return 0;
    }
  }

  public String getMisspelledWord() {
    return misspelledWord;
  }

  public int getMisspelledWordOffset() {
    return misspelledWordOffset;
  }

  public void setLexicons(Lexicon alexicon[]) {
    try {
      if (alexicon == null)
        throw new UnsupportedException();
      //      try
      //      {
      //        int i = 0xa7d1fa59;
      //        char c = '\213';
      //        char c1 = '\214';
      //        char c2 = '\u0287';
      //        char c3 = '\u03E7';
      //        char c4 = '\227';
      //        char c5 = '\u012B';
      //        int k = getOption(Integer.valueOf(32));
      //        if ((k & 0xff) == 162)
      //        {
      //          int i1 = (k & 0x1f00) >> 8;
      //          int j1 = (k & 0x1ffe000) >> 13;
      //          Date date = new Date();
      //          Date date1 = (new GregorianCalendar(2000, 0, 1)).getTime();
      //          Date date2 = new Date(date1.getTime() + (long)j1 * 0x5265c00L);
      //          long l3 = System.currentTimeMillis();
      //          long l5 = (long)getOption(Integer.valueOf(16)) * 1000L;
      //          int k2 = k & 0x7fffe000;
      //          int i3 = 0;
      //          for (int j3 = 0; j3 < 32; j3++)
      //          {
      //            i3 += k2 & 1;
      //            k2 >>= 1;
      //          }
      //
      //          if (i3 != i1)
      //            throw new Exception();
      //          if (date.after(date2))
      //          {
      //            if (l3 > l5 + 0xdbba0L)
      //            {
      //              String as[] = {
      //                "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\305\326\301\314\325\301\324\311\317\316\240\320\305\322\311\317\304\240\305\330\320\311\322\305\304", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\256", "\331\357\365\362\240\263\260\255\344\341\371\240\345\366\341\354\365\341\364\351\357\356\240\354\351\343\345\356\363\345\240\350\341\363\240\356\357\367\240\345\370\360\351\362\345\344\256", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
      //              };
      //              for (int k3 = 0; k3 < as.length; k3++)
      //              {
      //                for (int j4 = 0; j4 < as[k3].length(); j4++)
      //                  System.out.print((char)(as[k3].charAt(j4) & 0x7f));
      //
      //                System.out.println();
      //              }
      //
      //              setOption(Integer.valueOf(16), Integer.valueOf((int)(l3 / 1000L)));
      //            }
      //            throw new Exception();
      //          }
      //          if (l3 > l5 + 0xdbba0L)
      //          {
      //            String as1[] = {
      //              "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\306\317\322\240\305\326\301\314\325\301\324\311\317\316\240\317\316\314\331\240\255\240\316\317\324\240\306\317\322\240\320\322\317\304\325\303\324\311\317\316\240\325\323\305", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\241", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
      //            };
      //            for (int i4 = 0; i4 < as1.length; i4++)
      //            {
      //              for (int k4 = 0; k4 < as1[i4].length(); k4++)
      //                System.out.print((char)(as1[i4].charAt(k4) & 0x7f));
      //
      //              System.out.println();
      //            }
      //
      //            setOption(Integer.valueOf(16), Integer.valueOf((int)(l3 / 1000L)));
      //          }
      //        } else
      //        {
      //          long l1 = k ^ i;
      //          long l2 = 0L;
      //          for (int k1 = 0; k1 < 32; k1++)
      //          {
      //            long l4 = l1 >> k1 & 1L;
      //            l2 |= l4 << 31 - k1;
      //          }
      //
      //          int i2 = (int)(l2 / 10000L);
      //          if (i2 != c && i2 != c1)
      //            throw new Exception();
      //          int j2 = (int)(l2 % 10000L);
      //          if (i2 == c && j2 < c2 || j2 > c3)
      //            throw new Exception();
      //          if (i2 == c1 && (j2 < c4 || j2 > c5))
      //            throw new Exception();
      //        }
      //      }
      //      catch (Exception exception)
      //      {
      //        for (long l = System.currentTimeMillis(); System.currentTimeMillis() < l + 1000L;) {
      //          try {
      //            Thread.sleep(25);
      //          } catch (Throwable t) {
      //          }
      //        }
      //        throw new UnsupportedException();
      //      }
      lexicons = new Lexicon[alexicon.length];
      for (int j = 0; j < alexicon.length; j++)
        lexicons[j] = alexicon[j];

    }
    catch (UnsupportedException unsupportedexception) {
      lexicons = new Lexicon[0];
    }
  }

  public boolean setOption(int i, boolean flag) {
    boolean flag1 = (options & i) != 0;
    if (flag)
      options |= i;
    else
      options &= ~i;
    return flag1;
  }

  public static void setOption(Integer integer, Integer integer1) {
    if (opts == null)
      opts = new Hashtable();
    opts.put(integer, integer1);
  }

  public void suggest(String s, int i, WordComparator wordcomparator, SuggestionSet suggestionset) {
    String s1 = s;
    if ((options & 0x4000) != 0)
      s1 = stripPossessives(s);
    if (s1.length() == 0)
      return;
    for (int j = lexicons.length - 1; j >= 0; j--)
      if (lexicons[j] != null)
        lexicons[j].suggest(s1, i, wordcomparator, suggestionset);

    if ((options & 0x8000) != 0) {
      for (int k = 1; k < s1.length(); k++) {
        String s2 = s1.substring(0, k);
        String s3 = s1.substring(k);
        int k2 = options;
        options |= 0x200;
        if (check(s2) == 0 && check(s3) == 0) {
          String s8 = s2 + " " + s3;
          suggestionset.add(s8, wordcomparator.compare(s, s8));
        }
        options = k2;
      }

    }
    if ((options & 0x4000) != 0 && s1.length() >= 2) {
      char c = s1.charAt(s1.length() - 1);
      if (c == 's' || c == 'S' && !UniCharacter.isApostrophe(s1.charAt(s1.length() - 2))) {
        StringBuffer stringbuffer1 = new StringBuffer(s1);
        stringbuffer1.insert(stringbuffer1.length() - 1, '\'');
        if (check(stringbuffer1.toString()) != 16)
          suggestionset.add(stringbuffer1.toString(), wordcomparator.compare(s, stringbuffer1.toString()));
      }
    }
    StringBuffer stringbuffer = new StringBuffer(s1);
    for (int l = 0; l < stringbuffer.length() - 1; l++) {
      char c1 = stringbuffer.charAt(l);
      char c2 = stringbuffer.charAt(l + 1);
      char c3 = c1;
      char c4 = c2;
      if (UniCharacter.isUpperCase(c3) && UniCharacter.isLowerCase(c4)) {
        c3 = Character.toLowerCase(c3);
        c4 = Character.toUpperCase(c4);
      }
      stringbuffer.setCharAt(l, c4);
      stringbuffer.setCharAt(l + 1, c3);
      if ((check(stringbuffer.toString()) & 0x10) == 0)
        suggestionset.add(stringbuffer.toString(), wordcomparator.compare(s, stringbuffer.toString()));
      stringbuffer.setCharAt(l + 1, c2);
      stringbuffer.setCharAt(l, c1);
    }

    for (int i1 = 0; i1 < s1.length(); i1++) {
      String s4 = "";
      String s5 = "";
      if (i1 > 0)
        s4 = s1.substring(0, i1);
      if (i1 + 1 < s1.length())
        s5 = s1.substring(i1 + 1);
      String s9 = s4 + s5;
      if ((check(s9) & 0x10) == 0)
        suggestionset.add(s9, wordcomparator.compare(s, s9));
    }

    int j1 = 0;
    for (int k1 = 0; k1 < s1.length(); k1++)
      if (UniCharacter.isPunctuation(s1.charAt(k1)))
        j1++;

    if (j1 == 1) {
      int l1;
      for (l1 = 0; l1 < s1.length() && !UniCharacter.isPunctuation(s1.charAt(l1)); l1++);
      String s6 = null;
      if (l1 > 0)
        s6 = s1.substring(0, l1);
      String s10 = null;
      if (l1 < s1.length() - 1)
        s10 = s1.substring(l1 + 1);
      int i3 = options;
      options |= 0x200;
      if (s6 != null && s10 != null && check(s6) == 0 && check(s10) == 0) {
        StringBuffer stringbuffer3 = new StringBuffer(s6);
        stringbuffer3.append(' ');
        stringbuffer3.append(s10);
        suggestionset.add(stringbuffer3.toString(), wordcomparator.compare(s, stringbuffer3.toString()));
      }
      options = i3;
    }
    for (int i2 = 0; i2 < suggestionset.size(); i2++) {
      if (!suggestionset.getMatchCaseAt(i2))
        continue;
      String s7 = suggestionset.wordAt(i2);
      StringBuffer stringbuffer2 = new StringBuffer(s7);
      matchCase(stringbuffer2, false, (options & 0x20000) != 0, s);
      restorePossessives(stringbuffer2, s);
      if (!s7.equals(stringbuffer2.toString()))
        suggestionset.replaceAt(i2, stringbuffer2.toString());
    }

    for (int j2 = 0; j2 < suggestionset.size(); j2++) {
      for (int l2 = j2 + 1; l2 < suggestionset.size(); l2++)
        if (suggestionset.wordAt(j2).equals(suggestionset.wordAt(l2)))
          suggestionset.deleteAt(l2);

    }

  }

  public static String version() {
    String s = "$Revision: 1.4 $";
    StringTokenizer stringtokenizer = new StringTokenizer(s);
    stringtokenizer.nextToken();
    return stringtokenizer.nextToken();
  }

  private int actionToResultMask(int i) {
    switch (i) {
      case 65: // 'A'
      case 97: // 'a'
        return 1;

      case 67: // 'C'
      case 99: // 'c'
        return 2;

      case 101: // 'e'
        return 16;

      case 105: // 'i'
      default:
        return 0;
    }
  }

  private int checkWord2(String s, StringBuffer stringbuffer) {
    if (lexicons == null)
      return 16;
    if (ignoreWord(s))
      return 0;
    int i = checkWord3(s, s, stringbuffer);
    if (i != 0)
      return actionToResultMask(i);
    if ((options & 1) != 0 && s.length() > 0 && UniCharacter.isUpperCase(s.charAt(0))) {
      StringBuffer stringbuffer1 = new StringBuffer(s.length());
      stringbuffer1.append(Character.toLowerCase(s.charAt(0)));
      stringbuffer1.append(s.substring(1));
      int j = checkWord3(stringbuffer1.toString(), s, stringbuffer);
      if (j != 0)
        return actionToResultMask(j);
    }
    if ((options & 1) != 0) {
      boolean flag = true;
      int l = s.length();
      int i1 = 0;
      do {
        if (i1 >= l)
          break;
        if (UniCharacter.isLetter(s.charAt(i1)) && !UniCharacter.isUpperCase(s.charAt(i1))) {
          flag = false;
          break;
        }
        i1++;
      } while (true);
      if (flag) {
        int j1 = options;
        options &= -2;
        int k = checkWord3(s, s, stringbuffer);
        options = j1;
        if (k != 0)
          return actionToResultMask(k);
      }
    }
    if ((options & 0x20000) == 0 && UniCharacter.isUpperCase(s.charAt(0))) {
      StringBuffer stringbuffer2 = new StringBuffer(s);
      char c;
      do {
        int k1 = options;
        options |= 0x20000;
        int l1 = checkWord2(stringbuffer2.toString(), stringbuffer);
        options = k1;
        if ((l1 & 0x10) != 0)
          return l1;
        c = UniCharacter.nextAccentFromBase(UniCharacter.toBase(s.charAt(0)), stringbuffer2.charAt(0));
        if (c != 0)
          stringbuffer2.setCharAt(0, c);
      } while (c != 0);
    }
    return 16;
  }

  private int checkWord3(String s, String s1, StringBuffer stringbuffer) {
    for (int i = 0; i < lexicons.length; i++) {
      if (lexicons[i] == null)
        continue;
      int j = lexicons[i].findWord(s, (options & 1) != 0, stringbuffer);
      if (j == 0)
        continue;
      if (j == 65 || j == 67)
        matchCase(stringbuffer, true, (options & 0x20000) != 0, s1);
      return j;
    }

    return 0;
  }

  protected boolean containsSubWords(String s) {
    int i = s.length();
    for (int j = 0; j < i; j++) {
      char c = s.charAt(j);
      if ((options & 0x1000) != 0 && UniCharacter.isHyphen(c))
        return true;
      if ((options & 0x800) != 0 && UniCharacter.isApostrophe(c))
        return true;
    }

    return false;
  }

  protected int getSubWord(String s, StringBuffer stringbuffer) {
    int i = 0;
    stringbuffer.setLength(0);
    int j = s.length();
    int k = 0;
    do {
      if (k >= j)
        break;
      char c = s.charAt(k);
      if ((options & 0x1000) != 0 && UniCharacter.isHyphen(c)) {
        i++;
        break;
      }
      if ((options & 0x800) != 0 && UniCharacter.isApostrophe(c)) {
        i++;
        break;
      }
      stringbuffer.append(c);
      i++;
      k++;
    } while (true);
    return i;
  }

  protected boolean ignoreWord(String s) {
    int i = s.length();
    if ((options & 0x20) != 0) {
      boolean flag = false;
      for (int l = 0; l < i && !flag; l++)
        if (UniCharacter.isLetter(s.charAt(l)))
          flag = true;

      if (!flag)
        return true;
    }
    if ((options & 4) != 0 && UniCharacter.isUpperCase(s.charAt(0)))
      return true;
    if ((options & 8) != 0 && isMixedCase(s))
      return true;
    if ((options & 0x10) != 0) {
      for (int j = 0; j < i; j++)
        if (UniCharacter.isDigit(s.charAt(j)))
          return true;

    }
    if ((options & 2) != 0) {
      boolean flag1 = true;
      int i1 = 0;
      do {
        if (i1 >= i)
          break;
        if (!UniCharacter.isUpperCase(s.charAt(i1))) {
          flag1 = false;
          break;
        }
        i1++;
      } while (true);
      if (flag1 && i > 1)
        return true;
    }
    if ((options & 0x10000) != 0) {
      int k = s.lastIndexOf('.');
      if (k >= 0) {
        boolean flag2 = true;
        int k1 = 0;
        for (int l1 = k + 1; l1 < i && flag2 && k1 <= 4; l1++) {
          if (!UniCharacter.isLetterOrDigit(s.charAt(l1)))
            flag2 = false;
          k1++;
        }

        if (flag2 && k1 >= 2 && k1 <= 4)
          return true;
      }
      if (s.indexOf("://") >= 0)
        return true;
      int j1 = s.indexOf('/');
      if (j1 >= 0) {
        boolean flag3 = true;
        int i2 = 0;
        for (int j2 = j1 - 1; j2 >= 0 && i != 46 && flag3 && i2 <= 4; j2--) {
          if (!UniCharacter.isLetterOrDigit(s.charAt(j2)))
            flag3 = false;
          i2++;
        }

        if (flag3 && i2 >= 2 && i2 <= 4)
          return true;
      }
    }
    return false;
  }

  private boolean isMixedCase(String s) {
    boolean flag = false;
    int j = s.length();
    int i = 1;
    do {
      if (i >= j)
        break;
      if (UniCharacter.isLetter(s.charAt(i))) {
        flag = UniCharacter.isLowerCase(s.charAt(i));
        break;
      }
      i++;
    } while (true);
    if (i >= j)
      return false;
    for (; i < j; i++) {
      char c = s.charAt(i);
      if (UniCharacter.isLetter(c) && (flag && UniCharacter.isUpperCase(c) || !flag && UniCharacter.isLowerCase(c)))
        return true;
    }

    return false;
  }

  private void matchCase(StringBuffer stringbuffer, boolean flag, boolean flag1, String s) {
    int i = stringbuffer.length();
    int j = s.length();
    if (i == 0 || j == 0)
      return;
    if (flag) {
      int k = 0;
      for (int l = 0; l < i; l++) {
        char c1 = stringbuffer.charAt(l);
        if (UniCharacter.isUpperCase(s.charAt(k))) {
          c1 = Character.toUpperCase(c1);
          char c4;
          if (!flag1 && (c4 = UniCharacter.toBase(c1)) != c1)
            c1 = c4;
          stringbuffer.setCharAt(l, c1);
        } else {
          stringbuffer.setCharAt(l, Character.toLowerCase(stringbuffer.charAt(l)));
        }
        if (k + 1 < j)
          k++;
      }

    } else {
      boolean flag2 = true;
      int i1 = 0;
      do {
        if (i1 >= j)
          break;
        char c2 = s.charAt(i1);
        if (UniCharacter.isLetter(c2) && !UniCharacter.isUpperCase(c2)) {
          flag2 = false;
          break;
        }
        i1++;
      } while (true);
      if (!flag2 && UniCharacter.isUpperCase(s.charAt(0))) {
        char c = stringbuffer.charAt(0);
        stringbuffer.setCharAt(0, Character.toUpperCase(c));
        if (!flag1 && UniCharacter.toBase(c) != c)
          stringbuffer.setCharAt(0, UniCharacter.toBase(c));
      } else
        if (flag2 && s.length() > 1) {
          for (int j1 = 0; j1 < i; j1++) {
            char c3 = Character.toUpperCase(stringbuffer.charAt(j1));
            char c5;
            if (!flag1 && (c5 = UniCharacter.toBase(c3)) != c3)
              c3 = c5;
            stringbuffer.setCharAt(j1, c3);
          }

        }
    }
  }

  public static String stripPossessives(String s) {
    int i = s.length();
    if (i >= 2) {
      char c = s.charAt(i - 2);
      char c1 = s.charAt(i - 1);
      if (UniCharacter.isApostrophe(c) && Character.toLowerCase(c1) == 's')
        return s.substring(0, i - 2);
      if (Character.toLowerCase(c) == 's' && UniCharacter.isApostrophe(c1))
        return s.substring(0, i - 1);
    }
    return s;
  }

  public static void restorePossessives(StringBuffer stringbuffer, String s) {
    char c = '\0';
    char c1 = '\0';
    boolean flag = false;
    char c2 = '\0';
    char c3 = '\0';
    if (s.length() >= 2) {
      c2 = s.charAt(s.length() - 2);
      c3 = s.charAt(s.length() - 1);
    }
    if (UniCharacter.isApostrophe(c2) && Character.toLowerCase(c3) == 's') {
      flag = true;
      c = c3;
      c1 = c2;
    } else
      if (Character.toLowerCase(c2) == 's' && UniCharacter.isApostrophe(c3)) {
        flag = true;
        c = c2;
        c1 = c3;
      }
    if (flag)
      if (stringbuffer.length() > 0 && Character.toLowerCase(stringbuffer.charAt(stringbuffer.length() - 1)) == 's') {
        stringbuffer.append(c1);
      } else {
        stringbuffer.append(c1);
        stringbuffer.append(c);
      }
  }

}