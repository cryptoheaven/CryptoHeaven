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
// Source File Name:   EditableLexicon.java

package comx.Tiger.ssce;

import java.util.Enumeration;

// Referenced classes of package com.wintertree.ssce:
//      Lexicon, LexiconUpdateException, ParameterException, WordException

public interface EditableLexicon extends Lexicon {

  public abstract void addWord(String s, int i, String s1) throws LexiconUpdateException, ParameterException;

  public abstract void deleteWord(String s) throws WordException, LexiconUpdateException;

  public abstract int size();

  public abstract Enumeration words();
}