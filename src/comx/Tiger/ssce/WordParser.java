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
// Source File Name:   WordParser.java

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
