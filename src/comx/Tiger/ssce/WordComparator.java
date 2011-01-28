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
// Source File Name:   WordComparator.java

package comx.Tiger.ssce;

import java.io.Serializable;

public interface WordComparator extends Serializable {

  public static final int MAX_SCORE = 100;

  public abstract int compare(String s, String s1);
}