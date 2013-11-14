/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

import comx.Tiger.util.UniCharacter;
import java.util.NoSuchElementException;

public class StringWordParser implements WordParser {

  protected int cursor;
  protected boolean is1stWord;
  protected boolean isHyphenDelimiter;
  protected int nReplacements;
  protected int nWords;
  protected StringBuffer theString;
  protected int subWordLength;
  protected StringBuffer cachedWord;

  public StringWordParser(String s, boolean flag) {
    cursor = 0;
    is1stWord = true;
    isHyphenDelimiter = flag;
    nReplacements = 0;
    nWords = 0;
    if (s != null) {
      theString = new StringBuffer(s);
    } else {
      theString = null;
    }
    subWordLength = -1;
    cachedWord = new StringBuffer();
  }

  public StringWordParser(boolean flag) {
    this(null, flag);
  }

  public void deleteText(int i) throws NoSuchElementException {
    if (i > theString.length() - cursor) {
      throw new NoSuchElementException();
    } else {
      String s = theString.toString().substring(cursor + i);
      theString.setLength(cursor);
      theString.append(s);
      subWordLength = -1;
      cachedWord.setLength(0);
      return;
    }
  }

  public int deleteWord() throws NoSuchElementException {
    StringBuffer stringbuffer = new StringBuffer();
    return deleteWord(stringbuffer);
  }

  public int deleteWord(StringBuffer stringbuffer) throws NoSuchElementException {
    int i;
    for (i = cursor; i > 0 && UniCharacter.isWhitespace(theString.charAt(i - 1)); i--);
    String s = getWord();
    int j = (cursor - i) + s.length();
    stringbuffer.setLength(0);
    stringbuffer.append(theString.toString().substring(i, i + j));
    cursor = i;
    deleteText(j);
    for (; cursor < theString.length() && UniCharacter.isWhitespace(theString.charAt(cursor)); cursor++);
    return i;
  }

  public int getCursor() {
    return cursor;
  }

  public int getNumReplacements() {
    return nReplacements;
  }

  public int getNumWords() {
    return nWords;
  }

  public String getWord() throws NoSuchElementException {
    if (cachedWord.length() > 0) {
      return cachedWord.toString();
    }
    int i = findWordStart();
    if (i != cursor) {
      cursor = i;
      subWordLength = -1;
      cachedWord.setLength(0);
    }
    if (subWordLength > 0 && cursor + subWordLength < theString.length()) {
      cachedWord.setLength(0);
      cachedWord.append(theString.toString().substring(cursor, cursor + subWordLength));
      return cachedWord.toString();
    }
    int j = theString.length();
    if (cursor >= j) {
      throw new NoSuchElementException();
    }
    cachedWord.setLength(0);
    int k = 0;
    i = cursor;
    do {
      if (i >= j) {
        break;
      }
      char c = theString.charAt(i);
      if (c == '.' && isSurroundedByWordChars(theString, i)) {
        k++;
      }
      if (!includeCharInWord(theString, i, k > 0)) {
        break;
      }
      cachedWord.append(c);
      i++;
    } while (true);
    boolean flag = true;
    if (k > 0) {
      int l = 0;
      for (int i1 = 0; i1 < cachedWord.length(); i1++) {
        if (UniCharacter.isLetterOrDigit(cachedWord.charAt(i1))) {
          if (++l <= 2) {
            continue;
          }
          flag = false;
          break;
        }
        l = 0;
      }

      if (flag && i < j && theString.charAt(i) == '.') {
        cachedWord.append(theString.charAt(i));
        k++;
        i++;
      }
    }
    if (cachedWord.length() == 0) {
      return cachedWord.toString();
    }
    boolean flag1 = false;
    do {
      char c1 = cachedWord.charAt(cachedWord.length() - 1);
      flag1 = false;
      if (UniCharacter.isApostrophe(c1) && cachedWord.length() > 1 && Character.toLowerCase(cachedWord.charAt(cachedWord.length() - 2)) != 's') {
        cachedWord.setLength(cachedWord.length() - 1);
        flag1 = true;
      }
      if (c1 == '.' && (!flag || k == 1)) {
        cachedWord.setLength(cachedWord.length() - 1);
        flag1 = true;
      }
    } while (flag1);
    return cachedWord.toString();
  }

  public boolean hasMoreElements() {
    boolean flag = true;
    try {
      String s = getWord();
      if (s.length() == 0) {
        flag = false;
      }
    } catch (Exception exception) {
      flag = false;
    }
    return flag;
  }

  public void highlightWord() {
  }

  public void insertText(int i, String s) {
    theString.insert(i, s);
  }

  public boolean isDoubledWord(boolean flag) {
    String s = getWord();
    String s1 = getPrevWord();
    if (s1 == null) {
      return false;
    }
    boolean flag1;
    if (flag) {
      flag1 = s1.equals(s);
    } else {
      flag1 = s1.equalsIgnoreCase(s);
    }
    if (flag1) {
      char c = s1.charAt(s1.length() - 1);
      int i = cursor - 1;
      do {
        if (i < 0 || !flag1 || theString.charAt(i) == c) {
          break;
        }
        if (!UniCharacter.isWhitespace(theString.charAt(i))) {
          flag1 = false;
          break;
        }
        i--;
      } while (true);
    }
    return flag1;
  }

  public boolean isFirstWord() {
    return is1stWord;
  }

  public Object nextElement() {
    return nextWord();
  }

  public String nextWord() throws NoSuchElementException {
    int i = findWordStart();
    if (i != cursor) {
      cachedWord.setLength(0);
      cursor = i;
    }
    if (cursor >= theString.length()) {
      throw new NoSuchElementException();
    } else {
      String s = getWord();
      cursor += s.length();
      int nextWord = findWordStart();
      cursor = nextWord;
      is1stWord = false;
      nWords++;
      subWordLength = -1;
      cachedWord.setLength(0);
      return s;
    }
  }

  public void replaceWord(String s) throws NoSuchElementException {
    String s1 = getWord();
    String s2 = theString.toString().substring(cursor + s1.length());
    theString.setLength(cursor);
    theString.append(s);
    theString.append(s2);
    subWordLength = -1;
    cachedWord.setLength(0);
    nReplacements++;
  }

  public void setCursor(int i) throws StringIndexOutOfBoundsException {
    if (i < 0 || i >= theString.length()) {
      throw new StringIndexOutOfBoundsException(i);
    } else {
      cursor = i;
      subWordLength = -1;
      cachedWord.setLength(0);
      return;
    }
  }

  public void setText(String s) {
    if (theString == null) {
      theString = new StringBuffer();
    }
    theString.setLength(0);
    theString.append(s);
    cursor = 0;
    is1stWord = true;
    nReplacements = 0;
    nWords = 0;
    subWordLength = -1;
    cachedWord.setLength(0);
  }

  public void setWordLength(int i) {
    subWordLength = i;
    cachedWord.setLength(0);
  }

  public String toString() {
    return theString.toString();
  }

  protected int findWordStart() {
    int j = theString.length();
    int i;
    for (i = cursor; i < j && !is1stWordChar(theString.charAt(i)); i++);
    return i;
  }

  protected String getPrevWord() {
    int i;
    for (i = cursor - 1; i >= 0 && UniCharacter.isApostrophe(theString.charAt(i)); i--);
    for (; i >= 0 && !isWordChar(theString.charAt(i)); i--);
    if (i <= 0) {
      return null;
    }
    for (; i > 0 && isWordChar(theString.charAt(i - 1)); i--);
    int j = cursor;
    int k = subWordLength;
    String s = cachedWord.toString();
    cursor = i;
    subWordLength = -1;
    cachedWord.setLength(0);
    String s1 = getWord();
    cursor = j;
    subWordLength = k;
    cachedWord.setLength(0);
    cachedWord.append(s);
    return s1;
  }

  protected boolean includeCharInWord(StringBuffer stringbuffer, int i, boolean flag) {
    char c = stringbuffer.charAt(i);
    int j = stringbuffer.length();
    if (c == '.') {
      if (isSurroundedByWordChars(stringbuffer, i)) {
        return true;
      }
    } else if (c == '-' && !isHyphenDelimiter) {
      if (isSurroundedByWordChars(stringbuffer, i)) {
        return true;
      }
    } else if ('@' == c) {
      boolean flag1 = false;
      int k = i + 1;
      do {
        if (k >= j) {
          break;
        }
        char c1 = stringbuffer.charAt(k);
        if (UniCharacter.isWhitespace(c1)) {
          break;
        }
        if ('.' == c1 && isSurroundedByWordChars(stringbuffer, k)) {
          flag1 = true;
          break;
        }
        k++;
      } while (true);
      if (flag1 && isSurroundedByWordChars(stringbuffer, i)) {
        return true;
      }
    } else if (':' == c) {
      if (i > 0 && UniCharacter.isLetterOrDigit(stringbuffer.charAt(i - 1)) && i + 1 < j && stringbuffer.charAt(i + 1) == '/') {
        return true;
      }
    } else if ('/' == c) {
      if (flag) {
        return true;
      }
      if (i > 0 && (stringbuffer.charAt(i - 1) == ':' || stringbuffer.charAt(i - 1) == '/') && (i + 1 < j && stringbuffer.charAt(i + 1) == '/' || UniCharacter.isLetterOrDigit(stringbuffer.charAt(i + 1)))) {
        return true;
      }
    } else if ('&' == c || '%' == c || '+' == c || '=' == c || '?' == c) {
      if (flag) {
        return true;
      }
    } else if ('_' == c) {
      if (flag) {
        return true;
      }
      boolean flag2 = false;
      int l = i + 1;
      do {
        if (l >= j) {
          break;
        }
        char c2 = stringbuffer.charAt(l);
        if (UniCharacter.isWhitespace(c2)) {
          break;
        }
        if ('.' == c2 && isSurroundedByWordChars(stringbuffer, l)) {
          flag2 = true;
          break;
        }
        l++;
      } while (true);
      if (flag2) {
        return true;
      }
    } else if (isWordChar(c)) {
      return true;
    }
    return false;
  }

  /**
   * @deprecated Method includeCharInWord is deprecated
   */
  protected boolean includeCharInWord(char c, String s, int i, boolean flag) {
    return includeCharInWord(new StringBuffer(s), i, flag);
  }

  protected static boolean is1stWordChar(char c) {
    return UniCharacter.isLetterOrDigit(c);
  }

  private static boolean isSurroundedByWordChars(StringBuffer stringbuffer, int i) {
    return i > 0 && isWordChar(stringbuffer.charAt(i - 1)) && i + 1 < stringbuffer.length() && isWordChar(stringbuffer.charAt(i + 1));
  }

  protected static boolean isWordChar(char c) {
    return UniCharacter.isLetterOrDigit(c) || UniCharacter.isApostrophe(c);
  }
}