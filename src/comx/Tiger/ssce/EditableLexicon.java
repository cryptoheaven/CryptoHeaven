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

import java.util.Enumeration;

public interface EditableLexicon extends Lexicon {

  public abstract void addWord(String s, int i, String s1) throws LexiconUpdateException, ParameterException;

  public abstract void deleteWord(String s) throws WordException, LexiconUpdateException;

  public abstract int size();

  public abstract Enumeration words();
}