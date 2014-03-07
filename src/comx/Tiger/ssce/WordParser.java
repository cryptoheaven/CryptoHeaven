/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public interface WordParser extends Enumeration, Serializable {

  public abstract void deleteText(int i) throws NoSuchElementException;

  public abstract int deleteWord() throws NoSuchElementException;

  public abstract int deleteWord(StringBuffer stringbuffer) throws NoSuchElementException;

  public abstract int getCursor();

  public abstract int getNumReplacements();

  public abstract int getNumWords();

  public abstract String getWord() throws NoSuchElementException;

  public abstract void highlightWord();

  public abstract void insertText(int i, String s);

  public abstract boolean isDoubledWord(boolean flag) throws NoSuchElementException;

  public abstract boolean isFirstWord();

  public abstract String nextWord() throws NoSuchElementException;

  public abstract void replaceWord(String s) throws NoSuchElementException;

  public abstract void setCursor(int i) throws StringIndexOutOfBoundsException;

  public abstract void setWordLength(int i);
}